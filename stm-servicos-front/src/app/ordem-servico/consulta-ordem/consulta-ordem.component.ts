import { Component, OnInit } from '@angular/core';
import { OrdemServico } from '../ordem-servico';
import { OrdemServicoService } from '../ordem-servico.service';
import { ClienteService } from '../../clientes/cliente.service';
import { CredenciadoService } from '../../credenciados/credenciado.service';
import { debounceTime, forkJoin, map, Subject } from 'rxjs';
import { Router } from '@angular/router';
import { Solucao } from '../../solucao/solucao';
import { SolucaoService } from '../../solucao/solucao.service';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
  selector: 'app-consulta-ordem',
  standalone: false,
  templateUrl: './consulta-ordem.component.html',
  styleUrl: './consulta-ordem.component.scss'
})
export class ConsultaOrdemComponent implements OnInit {

  ordensServico: OrdemServico[] = [];
  totalElements: number = 0;
  page: number = 0;
  size: number = 10;
  totalPages: number = 0;
  loading = false;
  errorMessage = '';

  clientes: any[] = [];
  credenciados: any[] = [];

  private filtroSubject = new Subject<void>();

  modalSolucaoAberto = false;
  ordemSelecionadaId?: string;
  ordemSelecionada?: OrdemServico;

  filtro: any = {};

  solucao: Solucao = new Solucao();

  modalVisualizarSolucao = false;
  solucaoVisualizacao?: Solucao;

  modalEmail = false;
  textoEmailFormatado!: SafeHtml;

  pageSizes: number[] = [10, 25, 50, 100, 200];

  filtroMobileAberto = false;

  constructor(
    private service: OrdemServicoService,
    private clienteService: ClienteService,
    private solucaoService: SolucaoService,
    private credenciadoService: CredenciadoService,
    private router: Router,
    private sanitizer: DomSanitizer
  ) { }

  ngOnInit(): void {
    this.filtroSubject.pipe(debounceTime(400)).subscribe(() => {
      this.page = 0;
      this._carregarOrdens();
    });

    forkJoin({
      clientes: this.clienteService.listar().pipe(
        map(pageClientes => pageClientes.content ?? [])
      ),
      credenciados: this.credenciadoService.listar().pipe(
        map(pageCredenciados => pageCredenciados.content ?? [])
      )
    }).subscribe(({ clientes, credenciados }) => {
      this.clientes = clientes;
      this.credenciados = credenciados;
      this._carregarOrdens();
    });
  }

  ngOnDestroy(): void {
    this.filtroSubject.complete();
  }

  filtrosAtivos(): number {
    return Object.values(this.filtro).filter(
      (v: any) => v !== null && v !== undefined && v !== ''
    ).length;
  }

  limparFiltros(): void {
    this.filtro = {};
    this.page = 0;
    this._carregarOrdens();
  }

  _carregarOrdens() {
    this.loading = true;
    this.errorMessage = '';

    const filtroConvertido: any = { ...this.filtro };

    const dataISO = this.converterDataParaISO(this.filtro.dataAbertura);

    if (dataISO) {
      filtroConvertido.dataAbertura = dataISO;
    } else {
      delete filtroConvertido.dataAbertura;
    }

    this.service.listar(this.page, this.size, filtroConvertido).subscribe({
      next: res => {
        this.ordensServico = (res.content ?? [])
          .map(os => ({
            ...os,
            clienteNome: os.cliente?.razaoSocial || os.cliente?.nome || '-',
            credenciadoNome: os.credenciado?.rag || '-'
          }));

        this.totalElements = res.totalElements ?? this.ordensServico.length;
        this.totalPages = typeof res.totalPages === 'number' ? res.totalPages :
          Math.ceil(this.totalElements / this.size);
        this.loading = false;
      },
      error: err => {
        console.error('Erro ao carregar OS', err);
        this.errorMessage = 'Erro ao carregar Ordens de Serviço';
        this.loading = false;
      }
    });
  }

  carregarOrdens() {
    this.filtroSubject.next();
  }

  excluir(id?: string) {
    if (!id) return alert('ID inválido para exclusão.');

    if (confirm('Tem certeza que deseja excluir esta OS?')) {
      this.service.excluir(id.toString()).subscribe({
        next: () => this.carregarOrdens(),
        error: err => {
          console.error('Erro ao excluir OS', err);
          alert('Erro ao excluir Ordem de Serviço.');
        }
      });
    }
  }

  editar(id?: string) {
    this.router.navigate(['/ordem-servico/editar', id]);
  }

  verHistorico(id?: string) {
    if (!id) return;
    this.router.navigate(['/ordem-servico', id, 'historico']);
  }

  paginaAnterior() {
    if (this.page > 0) {
      this.page--;
      this._carregarOrdens();
    }
  }

  proximaPagina() {
    if ((this.page + 1) < this.totalPages) {
      this.page++;
      this._carregarOrdens();
    }
  }

  onSizeChange(event: Event) {
    const select = event.target as HTMLSelectElement;
    this.size = Number(select.value);
    this.page = 0;
    this._carregarOrdens();
  }

  abrirModalSolucao(id?: string) {
    if (!id) return;

    const os = this.ordensServico.find(o => o.id === id);
    if (!os) return;

    this.ordemSelecionada = os;
    this.ordemSelecionadaId = id;
    this.modalSolucaoAberto = true;
    this.solucao = new Solucao();
  }

  fecharModalSolucao() {
    this.modalSolucaoAberto = false;
    this.solucao = new Solucao();
    this.ordemSelecionada = undefined;
  }

  private toISO(dateTimeLocal?: string): string | undefined {
    if (!dateTimeLocal) return undefined;
    return new Date(dateTimeLocal).toISOString();
  }

  salvarSolucao() {
    if (!this.ordemSelecionadaId) return;

    if (!this.solucao.solucao || !this.solucao.horaInicial || !this.solucao.horaFinal) {
      alert('Preencha os campos obrigatórios.');
      return;
    }

    const payload: Solucao = {
      ...this.solucao,
      dataAtendimento: new Date().toISOString(),
      horaInicial: this.toISO(this.solucao.horaInicial),
      horaFinal: this.toISO(this.solucao.horaFinal)
    };

    this.service.finalizarOS(this.ordemSelecionadaId, payload)
      .subscribe({
        next: () => {
          alert('OS finalizada com sucesso!');
          this.fecharModalSolucao();
          this.carregarOrdens();
        },
        error: err => {
          console.error(err);
          alert('Erro ao finalizar OS');
        }
      });
  }

  abrirModalVisualizacao(ordemId: string) {
    this.solucaoService.buscarPorOrdem(ordemId).subscribe({
      next: solucao => {
        this.solucaoVisualizacao = solucao;
        this.modalVisualizarSolucao = true;
      },
      error: err => {
        console.error(err);
        alert('Erro ao carregar solução.');
      }
    });
  }

  fecharModalVisualizacao() {
    this.modalVisualizarSolucao = false;
    this.solucaoVisualizacao = undefined;
  }

  acaoSolucao(os: OrdemServico) {
    if (os.status === 'CONCLUIDA') {
      this.abrirModalVisualizacao(os.id!);
    } else {
      this.abrirModalSolucao(os.id);
    }
  }

  exportar(formato: 'xlsx' | 'csv' | 'pdf') {
    const chamada$ =
      formato === 'xlsx' ? this.service.exportarXlsx(this.filtro) :
        formato === 'csv' ? this.service.exportarCsv(this.filtro) :
          this.service.exportarPdf(this.filtro);

    chamada$.subscribe({
      next: (blob: Blob) => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `ordens-servico.${formato}`;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: err => {
        console.error(`Erro ao exportar ${formato}`, err);
        alert(`Erro ao exportar ${formato.toUpperCase()}`);
      }
    });
  }

  abrirModalEmail() {
    const s = this.solucaoVisualizacao;
    if (!s) return;

    const data = s.dataAtendimento
      ? new Date(s.dataAtendimento).toLocaleDateString('pt-BR')
      : '';

    const horaInicial = s.horaInicial
      ? new Date(s.horaInicial).toLocaleTimeString('pt-BR')
      : '';

    const horaFinal = s.horaFinal
      ? new Date(s.horaFinal).toLocaleTimeString('pt-BR')
      : '';

    const html = `
<table width="100%" cellpadding="0" cellspacing="0"
       style="font-family: Arial, sans-serif; font-size:14px; border-collapse: collapse;">
  <tr>
    <td style="background-color:#8ea6c9; padding:8px; font-weight:bold; color:#000;">
      POSICIONAMENTO DE ATENDIMENTO:
    </td>
  </tr>
  <tr>
    <td style="padding:8px;">
      <strong>OS:</strong> ${s.osClt ?? ''}
    </td>
  </tr>
  <tr>
    <td style="padding:8px;">
      <strong>Data:</strong> ${data}
      &nbsp;&nbsp; das ${horaInicial}
      &nbsp;&nbsp; às ${horaFinal}.
    </td>
  </tr>
  <tr>
    <td style="padding:8px;">
      <strong>Solução:</strong> ${s.solucao ?? ''}
    </td>
  </tr>
</table>
`;

    this.textoEmailFormatado = this.sanitizer.bypassSecurityTrustHtml(html);
    this.modalEmail = true;
  }

  async copiarTexto(element: HTMLElement) {
    try {
      const html = element.innerHTML;
      const blob = new Blob([html], { type: 'text/html' });
      const data = [new ClipboardItem({ 'text/html': blob })];
      await navigator.clipboard.write(data);
      alert('Texto formatado copiado com sucesso!');
    } catch (err) {
      await navigator.clipboard.writeText(element.innerText);
      alert('Texto copiado (modo simples).');
    }
  }

  /**
   * Converte data para ISO (yyyy-MM-dd) aceitando dois formatos:
   *
   * 1. ngx-mask SEM barras → valor bruto "ddMMyyyy" (ex: "10032025")
   *    O ngx-mask com mask="00/00/0000" guarda só os dígitos no ngModel.
   *
   * 2. Com barras → "dd/MM/yyyy" (ex: "10/03/2025")
   *    Compatibilidade com inputs sem máscara.
   *
   * Retorna undefined se a data estiver incompleta (menos de 8 dígitos).
   */
  private converterDataParaISO(data?: string): string | undefined {
    if (!data) return undefined;

    // Remove barras caso existam, ficando só com os dígitos
    const soDigitos = data.replace(/\//g, '');

    // Só prossegue se tiver exatamente 8 dígitos
    if (soDigitos.length !== 8 || isNaN(Number(soDigitos))) return undefined;

    const dia = soDigitos.substring(0, 2);
    const mes = soDigitos.substring(2, 4);
    const ano = soDigitos.substring(4, 8);

    return `${ano}-${mes}-${dia}`;
  }
}