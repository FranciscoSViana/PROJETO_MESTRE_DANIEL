import { Component, OnInit } from '@angular/core';
import { OrdemServico } from '../ordem-servico';
import { OrdemServicoService } from '../ordem-servico.service';
import { ClienteService } from '../../clientes/cliente.service';
import { CredenciadoService } from '../../credenciados/credenciado.service';
import { catchError, debounceTime, forkJoin, map, of, Subject } from 'rxjs';
import { Router } from '@angular/router';
import { Solucao } from '../../solucao/solucao';
import { SolucaoService } from '../../solucao/solucao.service';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { environment } from '../../../environments/environment';

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

  exportMenuOSAberto?: string;

  modalEmail = false;
  textoEmailFormatado!: SafeHtml;

  pageSizes: number[] = [10, 25, 50, 100, 200];

  filtroMobileAberto = false;
  modalRastreioAberto = false;
  ordemRastreioSelecionada?: OrdemServico;
  statusRastreioOpcoes: { value: string; descricao: string; cor: string }[] = [
    { value: 'POSTADO', descricao: 'Postado', cor: '#3B82F6' },
    { value: 'A_CAMINHO', descricao: 'A caminho', cor: '#F59E0B' },
    { value: 'CHEGOU', descricao: 'Chegou', cor: '#10B981' },
    { value: 'DEVOLVIDO', descricao: 'Devolvido', cor: '#EF4444' },
    { value: 'AGUARDANDO', descricao: 'Aguardando', cor: '#8B5CF6' },
  ];
  statusRastreioSelecionado?: string;

  copiarAposFinalizarOS: boolean = false;

  menuPagamentoAberto?: string;
  menuPagamentoPosicao = { top: 0, left: 0 };  // ✅ Inserir
  modalPagamentoAberto = false;
  ordemPagamentoSelecionada?: OrdemServico;

  pagamento: any = {
    km: null,
    valorChamado: null,
    valorKm: null,
    pedagio: null,
    estacionamento: null,
    outros: '',
    valorOutros: null,
    lote: '',
    cpfNf: '',
    tipoPagamento: '',
    banco: '',
    urlComprovante: '',
    chavePix: ''
  };

  carregandoPagamento = false;

  uploadandoComprovante = false;

  pagamentoModoEdicao = false; // ✅ novo controle

  menuPagamentoDirecao: 'up' | 'down' = 'down';

  constructor(
    private service: OrdemServicoService,
    private clienteService: ClienteService,
    private solucaoService: SolucaoService,
    private credenciadoService: CredenciadoService,
    private router: Router,
    private sanitizer: DomSanitizer
  ) { }

  ngOnInit(): void {
    window.addEventListener('scroll', () => {
      this.menuPagamentoAberto = undefined;
    }, true);

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
    this.copiarAposFinalizarOS = false; // reset
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

    const copiar = this.copiarAposFinalizarOS;
    const ordemParaCopiar = { ...this.ordemSelecionada };

    this.service.finalizarOS(this.ordemSelecionadaId, payload)
      .subscribe({
        next: () => {
          this.fecharModalSolucao();
          this.carregarOrdens();

          if (copiar) {
            this.router.navigate(['/ordem-servico/cadastro'], {
              state: { copiarDe: ordemParaCopiar }
            });
          } else {
            alert('OS finalizada com sucesso!');
          }
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

  abrirModalRastreio(os: OrdemServico) {
    if (!os.rastreio) return; // só abre se tiver rastreio
    this.ordemRastreioSelecionada = os;
    this.statusRastreioSelecionado = os.statusRastreio;
    this.modalRastreioAberto = true;
  }

  fecharModalRastreio() {
    this.modalRastreioAberto = false;
    this.ordemRastreioSelecionada = undefined;
    this.statusRastreioSelecionado = undefined;
  }

  salvarStatusRastreio() {
    const id = this.ordemRastreioSelecionada?.id;
    if (!id || !this.statusRastreioSelecionado) return;

    this.service.atualizarStatusRastreio(id, this.statusRastreioSelecionado).subscribe({
      next: () => {
        this.fecharModalRastreio();
        this.carregarOrdens();
      },
      error: err => {
        console.error(err);
        alert('Erro ao atualizar status do rastreio.');
      }
    });
  }

  // Helper para buscar a cor do status atual
  getCorRastreio(statusRastreio?: string): string {
    return this.statusRastreioOpcoes
      .find(s => s.value === statusRastreio)?.cor ?? '#9CA3AF';
  }

  imprimirOS(os: OrdemServico, formato: 'pdf' | 'xlsx') {
    const chamada$ = formato === 'pdf'
      ? this.service.relatorioIndividualPdf(os.id!)
      : this.service.relatorioIndividualXlsx(os.id!);

    chamada$.subscribe({
      next: (blob: Blob) => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `OS-${os.osClt ?? os.id}.${formato}`;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: err => {
        console.error('Erro ao gerar relatório', err);
        alert('Erro ao gerar relatório da OS.');
      }
    });
  }

  toggleExportMenuOS(id: string) {
    this.exportMenuOSAberto = this.exportMenuOSAberto === id ? undefined : id;
  }

  fecharExportMenuOS() {
    this.exportMenuOSAberto = undefined;
  }

  togglePagamentoMenu(os: OrdemServico, event: MouseEvent) {
    if (this.menuPagamentoAberto === os.id) {
      this.menuPagamentoAberto = undefined;
      return;
    }

    const botao = (event.currentTarget as HTMLElement).getBoundingClientRect();
    const alturaMenu = 80; // altura aproximada do menu (2 itens)
    const espacoAbaixo = window.innerHeight - botao.bottom;

    const top = espacoAbaixo < alturaMenu
      ? botao.top - alturaMenu        // abre para cima
      : botao.bottom + 4;             // abre para baixo

    this.menuPagamentoPosicao = {
      top,
      left: botao.right - 176         // 176 = w-44
    };

    this.menuPagamentoAberto = os.id;
  }

  fecharMenuPagamento() {
    this.menuPagamentoAberto = undefined;
  }

  abrirModalPagamento(os: OrdemServico) {
    this.ordemPagamentoSelecionada = os;
    this.pagamento = {
      km: null, valorChamado: null, valorKm: null,
      pedagio: null, estacionamento: null, outros: '',
      valorOutros: null, lote: '', cpfNf: '',
      tipoPagamento: '', banco: '', urlComprovante: ''
    };
    this.carregandoPagamento = true;
    this.modalPagamentoAberto = true;

    // 1️⃣ Valores padrão do credenciado
    const credenciadoId = os.credenciado?.id;
    if (credenciadoId) {
      this.credenciadoService.buscarPorId(credenciadoId).subscribe({
        next: (credenciado) => {
          if (credenciado.valorChamado != null)
            this.pagamento.valorChamado = credenciado.valorChamado;
          if (credenciado.valorKm != null)
            this.pagamento.valorKm = credenciado.valorKm;

          // ✅ Inserir:
          if (credenciado.tipoFluxoPagamento)
            this.pagamento.lote = credenciado.tipoFluxoPagamento;
          if (credenciado.chavePix)
            this.pagamento.chavePix = credenciado.chavePix;
        },
        error: () => { }
      });
    }

    // 2️⃣ Pagamento existente + Solução da OS em paralelo
    // 2️⃣ Pagamento existente + Solução da OS em paralelo
    forkJoin({
      pagamento: this.service.buscarPagamento(os.id!).pipe(
        catchError(() => of(null))
      ),
      solucao: this.solucaoService.buscarPorOrdem(os.id!).pipe(
        catchError(() => of(null))   // ✅ captura o erro 404/500 do back
      )
    }).subscribe({
      next: ({ pagamento: pag, solucao }) => {

        // ✅ Se não tem solução e não tem pagamento, bloqueia e avisa
        if (!solucao && !pag) {
          this.carregandoPagamento = false;
          this.modalPagamentoAberto = false;
          alert('Esta OS ainda não possui solução registrada. Finalize a OS antes de registrar o pagamento.');
          return;
        }

        if (pag) {
          this.pagamento = {
            km: pag.km,
            valorChamado: pag.valorChamado,
            valorKm: pag.valorKm,
            pedagio: pag.pedagio,
            estacionamento: pag.estacionamento,
            outros: pag.outros ?? '',
            valorOutros: pag.valorOutros,
            lote: pag.lote ?? '',
            cpfNf: pag.cpfNf ?? '',
            tipoPagamento: pag.tipoPagamento ?? '',
            banco: pag.banco ?? '',
            urlComprovante: pag.urlComprovante ?? '',
            chavePix: pag.chavePix ?? this.pagamento.chavePix ?? ''
          };
          this.pagamentoModoEdicao = false;
        } else {
          if (solucao) {
            this.pagamento.km = solucao.km;
            this.pagamento.pedagio = solucao.pedagio;
            this.pagamento.estacionamento = solucao.estacionamento;
            this.pagamento.valorOutros = solucao.outros;
          }
          this.pagamentoModoEdicao = true;
        }
        this.carregandoPagamento = false;
      },
      error: () => {
        this.carregandoPagamento = false;
      }
    });
  }

  fecharModalPagamento() {
    this.modalPagamentoAberto = false;
    this.pagamento = {};
    this.pagamentoModoEdicao = false; // ✅ reset
  }

  salvarPagamento() {
    if (!this.ordemPagamentoSelecionada?.id) return;
    if (!this.pagamento.tipoPagamento) {
      alert('Selecione o tipo de pagamento.');
      return;
    }

    const payload = {
      tipoPagamento: this.pagamento.tipoPagamento,
      lote: this.pagamento.lote,
      cpfNf: this.pagamento.cpfNf,
      banco: this.pagamento.banco,
      urlComprovante: this.pagamento.urlComprovante,

      // 🔥 ESSENCIAL
      valorChamado: this.toNumber(this.pagamento.valorChamado),
      km: this.toNumber(this.pagamento.km),
      valorKm: this.toNumber(this.pagamento.valorKm),
      pedagio: this.toNumber(this.pagamento.pedagio),
      estacionamento: this.toNumber(this.pagamento.estacionamento),
      valorOutros: this.toNumber(this.pagamento.valorOutros),
      outros: this.pagamento.outros,
      chavePix: this.pagamento.chavePix
    };

    // ✅ Se já tem pagamento salvo (pago = true) usa PUT, senão POST
    const chamada$ = this.pagamento.pago
      ? this.service.editarPagamento(this.ordemPagamentoSelecionada.id, payload)
      : this.service.registrar(this.ordemPagamentoSelecionada.id, payload);

    chamada$.subscribe({
      next: () => {
        alert('Pagamento salvo com sucesso!');
        this.pagamentoModoEdicao = false;
        this.fecharModalPagamento();
        this.carregarOrdens();
      },
      error: err => {
        console.error(err);
        alert('Erro ao salvar pagamento.');
      }
    });
  }

  toNumber(value: any): number {
    if (value === null || value === undefined || value === '') return 0;
    return Number(value);
  }

  onTipoPagamentoChange() {
    if (this.pagamento.tipoPagamento !== 'PIX') {
      this.pagamento.chavePix = '';
    }
  }

  habilitarEdicaoPagamento() {
    this.pagamentoModoEdicao = true;
  }

  calcularTotalPreview(): number {
    const p = this.pagamento;

    return (p.valorChamado || 0) +
      ((p.km || 0) * (p.valorKm || 0)) +
      (p.pedagio || 0) +
      (p.estacionamento || 0) +
      (p.valorOutros || 0);
  }

  // No componente, ao selecionar o arquivo:
  async uploadComprovante(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;

    const osg = this.ordemPagamentoSelecionada?.osg;
    if (!osg) {
      alert('OSG não encontrada para nomear o comprovante.');
      return;
    }

    this.uploadandoComprovante = true;

    this.service.uploadComprovante(file, osg).subscribe({
      next: (url) => {
        this.pagamento.urlComprovante = url;
        this.uploadandoComprovante = false;
      },
      error: () => {
        alert('Erro ao fazer upload do comprovante.');
        this.uploadandoComprovante = false;
      }
    });
  }

  removerComprovante() {
    const url = this.pagamento.urlComprovante;
    if (!url) return;

    this.service.deletarComprovante(url).subscribe({
      next: () => {
        this.pagamento.urlComprovante = '';
      },
      error: () => {
        // Mesmo com erro no S3, limpa o campo localmente
        // para não bloquear o usuário
        this.pagamento.urlComprovante = '';
        console.warn('Arquivo pode não ter sido removido do S3.');
      }
    });
  }

  async downloadComprovante() {
    const url = this.pagamento.urlComprovante as string;
    if (!url) return;
    window.open(url, '_blank');
  }

  calcularTotalKm(): number {
    const km = this.pagamento?.km || 0;
    const valorKm = this.pagamento?.valorKm || 0;
    return km * valorKm;
  }

  getOrdemById(id: string): OrdemServico {
    return this.ordensServico.find(o => o.id === id)!;
  }
}