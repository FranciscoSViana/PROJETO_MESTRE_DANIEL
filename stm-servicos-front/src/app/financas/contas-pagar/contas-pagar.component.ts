import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';
import { Chart, registerables } from 'chart.js';

import { FinancasService } from '../financas.service';
import { Page } from '../../template/utils/page';
import { ContasPagarItem } from '../contas-pagar-item';

Chart.register(...registerables);

@Component({
  selector: 'app-contas-pagar',
  standalone: false,
  templateUrl: './contas-pagar.component.html',
  styleUrls: ['./contas-pagar.component.scss']
})
export class ContasPagarComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();
  private filterChange$ = new Subject<void>();
  private chartInstance: Chart | null = null;

  items: ContasPagarItem[] = [];
  loading = false;
  exportingXlsx = false;
  exportingPdf = false;
  filtroMobileAberto = false;

  page = 0;
  size = 20;
  totalPages = 0;
  totalElements = 0;
  pageSizes = [10, 20, 50, 100];

  filtro = {
    osg: '',
    osClt: '',
    cliente: '',
    credenciado: '',
    pago: '' as '' | 'true' | 'false',
    lote: '',
    dataAberturaInicio: '',
    dataAberturaFim: '',
    dataPagamentoInicio: '',
    dataPagamentoFim: '',
  };

  loteOptions: string[] = [];

  pagoOptions = [
    { label: 'Todos', value: '' },
    { label: 'Pago', value: 'true' },
    { label: 'Não Pago / Sem pagamento', value: 'false' }
  ];

  expandedId: string | null = null;

  totalPago = 0;
  totalNaoPago = 0;
  totalGeral = 0;

  constructor(private financasService: FinancasService) { }

  ngOnInit(): void {
    this.filterChange$
      .pipe(debounceTime(400), takeUntil(this.destroy$))
      .subscribe(() => {
        this.page = 0;
        this.carregar();
      });

    this.carregar();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.chartInstance?.destroy();
  }

  carregar(): void {
    this.loading = true;

    this.financasService.listar(this.page, this.size, this.filtro)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res: Page<ContasPagarItem>) => {
          this.items = res.content.map(i => ({
            ...i,
            valorTotal: i.valorTotal ?? 0,
            valorChamado: i.valorChamado ?? 0,
            km: i.km ?? 0,
            valorKm: i.valorKm ?? 0,
            pedagio: i.pedagio ?? 0,
            estacionamento: i.estacionamento ?? 0,
            valorOutros: i.valorOutros ?? 0,
            pago: i.pago ?? false
          }));

          const lotesUnicos = new Set<string>();
          this.items.forEach(i => { if (i.lote) lotesUnicos.add(i.lote); });
          const novasOpcoes = Array.from(lotesUnicos).sort();
          novasOpcoes.forEach(l => {
            if (!this.loteOptions.includes(l)) this.loteOptions.push(l);
          });
          this.loteOptions.sort();

          this.totalPages = res.totalPages;
          this.totalElements = res.totalElements;

          this.calcularTotais();
          this.renderChart();
          this.loading = false;
        },
        error: () => (this.loading = false)
      });
  }

  onFiltroChange(): void {
    this.filterChange$.next();
  }

  onSelectFiltroChange(): void {
    this.page = 0;
    this.carregar();
  }

  calcularTotais(): void {
    this.totalPago = this.items.filter(i => i.pago).reduce((acc, i) => acc + (i.valorTotal ?? 0), 0);
    this.totalNaoPago = this.items.filter(i => !i.pago).reduce((acc, i) => acc + (i.valorTotal ?? 0), 0);
    this.totalGeral = this.totalPago + this.totalNaoPago;
  }

  renderChart(): void {
    setTimeout(() => {
      const canvas = document.getElementById('chartPizza') as HTMLCanvasElement;
      if (!canvas) return;
      this.chartInstance?.destroy();

      const pago = this.items.filter(i => i.pago).length;
      const naoPago = this.items.filter(i => !i.pago).length;

      this.chartInstance = new Chart(canvas, {
        type: 'doughnut',
        data: {
          labels: ['Pago', 'Não Pago'],
          datasets: [{
            data: [pago, naoPago],
            backgroundColor: ['#22c55e', '#ef4444'],
            borderColor: ['#16a34a', '#dc2626'],
            borderWidth: 2,
            hoverOffset: 8
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          cutout: '65%',
          plugins: {
            legend: { position: 'bottom', labels: { font: { size: 11 }, padding: 10 } },
            tooltip: { callbacks: { label: (ctx) => ` ${ctx.label}: ${ctx.parsed} OS` } }
          }
        }
      });
    }, 100);
  }

  toggleExpand(id?: string): void {
    if (!id) return;
    this.expandedId = this.expandedId === id ? null : id;
  }

  paginaAnterior(): void {
    if (this.page > 0) { this.page--; this.carregar(); }
  }

  proximaPagina(): void {
    if (this.page + 1 < this.totalPages) { this.page++; this.carregar(); }
  }

  onSizeChange(event: Event): void {
    this.size = Number((event.target as HTMLSelectElement).value);
    this.page = 0;
    this.carregar();
  }

  limparFiltros(): void {
    this.filtro = {
      osg: '', osClt: '', cliente: '', credenciado: '',
      pago: '', lote: '',
      dataAberturaInicio: '', dataAberturaFim: '',
      dataPagamentoInicio: '', dataPagamentoFim: '',
    };
    this.loteOptions = []; // limpa também as opções de lote acumuladas
    this.page = 0;
    this.carregar();
  }

  exportarXlsx(): void {
    this.exportingXlsx = true;
    this.financasService.exportarXlsx(this.filtro).subscribe({
      next: (blob) => { this.downloadBlob(blob, 'contas-pagar.xlsx'); this.exportingXlsx = false; },
      error: () => (this.exportingXlsx = false)
    });
  }

  exportarPdf(): void {
    this.exportingPdf = true;
    this.financasService.exportarPdf(this.filtro).subscribe({
      next: (blob) => { this.downloadBlob(blob, 'contas-pagar.pdf'); this.exportingPdf = false; },
      error: () => (this.exportingPdf = false)
    });
  }

  formatCurrency(val?: number): string {
    return (val ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  formatDate(dt?: string): string {
    if (!dt) return '-';
    // Evita deslocamento de fuso: extrai direto da string ISO
    const part = dt.substring(0, 10); // "2026-03-24"
    const [y, m, d] = part.split('-');
    return `${d}/${m}/${y}`;
  }

  private downloadBlob(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = filename; a.click();
    URL.revokeObjectURL(url);
  }
}