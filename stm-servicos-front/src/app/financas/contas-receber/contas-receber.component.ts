import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { debounceTime, takeUntil } from 'rxjs/operators';
import { Chart, registerables } from 'chart.js';
import { FinancasReceberService } from '../financas-receber.service';
import { Page } from '../../template/utils/page';
import { ContasReceberItem } from '../contas-receber-item';
import { ContasReceberTotais } from '../contas-receber-totais';

Chart.register(...registerables);

@Component({
  selector: 'app-contas-receber',
  standalone: false,
  templateUrl: './contas-receber.component.html',
  styleUrls: ['./contas-receber.component.scss']
})
export class ContasReceberComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();
  private filterChange$ = new Subject<void>();
  private chartInstance: Chart | null = null;

  items: ContasReceberItem[] = [];
  totais: ContasReceberTotais | null = null;

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
    lote: '',
    recebido: '' as '' | 'true' | 'false',
    dataAberturaInicio: '',
    dataAberturaFim: '',
    dataPagamentoInicio: '',
    dataPagamentoFim: '',
  };

  loteOptions: string[] = [];

  recebidoOptions = [
    { label: 'Todos', value: '' },
    { label: 'Recebido', value: 'true' },
    { label: 'Pendente', value: 'false' }
  ];

  expandedId: string | null = null;

  totalRecebido = 0;
  totalNaoRecebido = 0;
  totalGeral = 0;

  // ── Estado do modal de pagamento em lote ──────────────────────────────────
  modalPagamentoLoteAberto = false;
  etapaLote: 1 | 2 = 1;  // 1 = seleção cliente/lote, 2 = preenchimento dados

  clientesDisponiveis: string[] = [];
  lotesDoCliente: string[] = [];
  osPendentesLote: ContasReceberItem[] = [];

  clienteSelecionadoLote = '';
  loteSelecionadoLote = '';
  carregandoLotes = false;
  carregandoOsPendentes = false;
  uploadandoComprovanteLote = false;

  pagamentoLote: any = {
    tipoPagamento: '',
    lote: '',
    nf: '',
    banco: '',
    urlComprovante: '',
    dataPrevista: null,
    dataPagamento: null
  };

  constructor(private service: FinancasReceberService) { }

  ngOnInit(): void {
    this.filterChange$
      .pipe(debounceTime(400), takeUntil(this.destroy$))
      .subscribe(() => { this.page = 0; this.carregar(); });

    this.service.listarLotes()
      .pipe(takeUntil(this.destroy$))
      .subscribe(lotes => { this.loteOptions = lotes.sort(); });

    this.carregar();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.chartInstance?.destroy();
  }

  carregar(): void {
    this.loading = true;

    this.service.listar(this.page, this.size, this.filtro)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (res: Page<ContasReceberItem>) => {
          this.items = res.content.map(i => ({
            ...i,
            valorTotal: i.valorTotal ?? 0,
            valorChamado: i.valorChamado ?? 0,
            km: i.km ?? 0,
            valorKm: i.valorKm ?? 0,
            pedagio: i.pedagio ?? 0,
            estacionamento: i.estacionamento ?? 0,
            valorOutros: i.valorOutros ?? 0,
            recebido: i.recebido ?? false,
            pago: i.pago ?? false,
          }));
          this.totalPages = res.totalPages;
          this.totalElements = res.totalElements;
          this.loading = false;
        },
        error: () => (this.loading = false)
      });

    this.service.totais(this.filtro)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (t: ContasReceberTotais) => {
          this.totais = t;
          this.totalRecebido = t.totalRecebido ?? 0;
          this.totalNaoRecebido = t.totalNaoRecebido ?? 0;
          this.totalGeral = t.totalGeral ?? 0;
          this.renderChart(t.qtdRecebido, t.qtdNaoRecebido);
        },
        error: () => { }
      });
  }

  onFiltroChange(): void { this.filterChange$.next(); }

  onSelectFiltroChange(): void { this.page = 0; this.carregar(); }

  limparFiltros(): void {
    this.filtro = {
      osg: '', osClt: '', cliente: '', lote: '', recebido: '',
      dataAberturaInicio: '', dataAberturaFim: '',
      dataPagamentoInicio: '', dataPagamentoFim: '',
    };
    this.loteOptions = [];
    this.page = 0;
    this.carregar();
  }

  renderChart(recebido: number, naoRecebido: number): void {
    setTimeout(() => {
      const canvas = document.getElementById('chartReceber') as HTMLCanvasElement;
      if (!canvas) return;
      this.chartInstance?.destroy();
      this.chartInstance = new Chart(canvas, {
        type: 'doughnut',
        data: {
          labels: ['Recebido', 'Pendente'],
          datasets: [{
            data: [recebido, naoRecebido],
            backgroundColor: ['#22c55e', '#f97316'],
            borderColor: ['#16a34a', '#ea580c'],
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

  paginaAnterior(): void { if (this.page > 0) { this.page--; this.carregar(); } }
  proximaPagina(): void { if (this.page + 1 < this.totalPages) { this.page++; this.carregar(); } }

  onSizeChange(event: Event): void {
    this.size = Number((event.target as HTMLSelectElement).value);
    this.page = 0;
    this.carregar();
  }

  exportarXlsx(): void {
    this.exportingXlsx = true;
    this.service.exportarXlsx(this.filtro).subscribe({
      next: (blob) => { this.downloadBlob(blob, 'contas-receber.xlsx'); this.exportingXlsx = false; },
      error: () => (this.exportingXlsx = false)
    });
  }

  exportarPdf(): void {
    this.exportingPdf = true;
    this.service.exportarPdf(this.filtro).subscribe({
      next: (blob) => { this.downloadBlob(blob, 'contas-receber.pdf'); this.exportingPdf = false; },
      error: () => (this.exportingPdf = false)
    });
  }

  formatCurrency(val?: number): string {
    return (val ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  formatDate(dt?: string): string {
    if (!dt) return '-';
    const part = dt.substring(0, 10);
    const [y, m, d] = part.split('-');
    return `${d}/${m}/${y}`;
  }

  private downloadBlob(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = filename; a.click();
    URL.revokeObjectURL(url);
  }

  // ── Abre o modal ────────────────────────────────────────────────────────────
  abrirModalPagamentoLote(): void {
    this.modalPagamentoLoteAberto = true;
    this.etapaLote = 1;
    this.clienteSelecionadoLote = '';
    this.loteSelecionadoLote = '';
    this.osPendentesLote = [];
    this.lotesDoCliente = [];
    this.pagamentoLote = {
      tipoPagamento: '', lote: '', nf: '',
      banco: '', urlComprovante: '',
      dataPrevista: null, dataPagamento: null
    };

    // Extrai clientes distintos dos itens já carregados na tela
    const set = new Set(this.items.map(i => i.cliente).filter(Boolean) as string[]);
    this.clientesDisponiveis = Array.from(set).sort();
  }

  fecharModalPagamentoLote(): void {
    this.modalPagamentoLoteAberto = false;
  }

  // ── Ao selecionar cliente → busca lotes pendentes ───────────────────────────
  onClienteLoteChange(): void {
    if (!this.clienteSelecionadoLote) { this.lotesDoCliente = []; return; }
    this.carregandoLotes = true;
    this.loteSelecionadoLote = '';
    this.osPendentesLote = [];

    this.service.listarLotesPorCliente(this.clienteSelecionadoLote).subscribe({
      next: (lotes) => {
        this.lotesDoCliente = lotes.sort();
        this.carregandoLotes = false;
      },
      error: () => (this.carregandoLotes = false)
    });
  }

  // ── Ao selecionar lote → carrega OS pendentes ────────────────────────────────
  onLoteLoteChange(): void {
    if (!this.clienteSelecionadoLote || !this.loteSelecionadoLote) return;
    this.carregandoOsPendentes = true;
    this.pagamentoLote.lote = this.loteSelecionadoLote;

    this.service.listarOsPendentes(this.clienteSelecionadoLote, this.loteSelecionadoLote).subscribe({
      next: (os) => {
        this.osPendentesLote = os;
        this.carregandoOsPendentes = false;
      },
      error: () => (this.carregandoOsPendentes = false)
    });
  }

  // ── Avança para etapa 2 ──────────────────────────────────────────────────────
  avancarEtapa(): void {
    if (!this.clienteSelecionadoLote || !this.loteSelecionadoLote || !this.osPendentesLote.length) {
      alert('Selecione um cliente, lote e aguarde as OS serem carregadas.');
      return;
    }
    this.etapaLote = 2;
  }

  // ── Upload comprovante do lote ───────────────────────────────────────────────
  async uploadComprovanteLote(event: Event): Promise<void> {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    this.uploadandoComprovanteLote = true;
    this.service.uploadComprovanteRecebimento(file, this.loteSelecionadoLote).subscribe({
      next: (url) => { this.pagamentoLote.urlComprovante = url; this.uploadandoComprovanteLote = false; },
      error: () => { alert('Erro ao fazer upload do comprovante.'); this.uploadandoComprovanteLote = false; }
    });
  }

  // ── Confirma pagamento em lote ───────────────────────────────────────────────
  confirmarPagamentoLote(): void {
    if (!this.pagamentoLote.tipoPagamento) {
      alert('Selecione o tipo de pagamento.');
      return;
    }

    const ids = this.osPendentesLote
      .map(os => os.ordemServicoId)
      .filter(Boolean) as string[];

    if (!ids.length) {
      alert('Nenhuma OS selecionada para pagamento.');
      return;
    }

    const payload = {
      ordemServicoIds: ids,
      tipoPagamento: this.pagamentoLote.tipoPagamento,
      lote: this.pagamentoLote.lote,
      nf: this.pagamentoLote.nf,
      banco: this.pagamentoLote.banco,
      urlComprovante: this.pagamentoLote.urlComprovante,
      dataPrevista: this.pagamentoLote.dataPrevista || null,
      dataPagamento: this.pagamentoLote.dataPagamento || null
    };

    this.service.registrarPagamentoLote(payload).subscribe({
      next: (resultado) => {
        const msg = resultado.totalErro > 0
          ? `Processado: ${resultado.totalProcessado} | Sucesso: ${resultado.totalSucesso} | Erros: ${resultado.totalErro}\n\n${resultado.erros.join('\n')}`
          : `${resultado.totalSucesso} OS registradas com sucesso!`;
        alert(msg);
        this.fecharModalPagamentoLote();
        this.carregar();
      },
      error: () => alert('Erro ao registrar pagamento em lote.')
    });
  }

  calcularTotalLote(): number {
    return this.osPendentesLote.reduce((acc, os) => acc + (os.valorTotal ?? 0), 0);
  }
}