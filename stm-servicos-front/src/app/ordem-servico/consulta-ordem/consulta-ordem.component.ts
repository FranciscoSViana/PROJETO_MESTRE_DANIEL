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
    { value: 'POSTADO',    descricao: 'Postado',    cor: '#3B82F6' },
    { value: 'A_CAMINHO',  descricao: 'A caminho',  cor: '#F59E0B' },
    { value: 'CHEGOU',     descricao: 'Chegou',     cor: '#10B981' },
    { value: 'DEVOLVIDO',  descricao: 'Devolvido',  cor: '#EF4444' },
    { value: 'AGUARDANDO', descricao: 'Aguardando', cor: '#8B5CF6' },
  ];
  statusRastreioSelecionado?: string;

  copiarAposFinalizarOS: boolean = false;

  menuPagamentoAberto?: string;
  menuPagamentoPosicao = { top: 0, left: 0 };
  modalPagamentoAberto = false;
  ordemPagamentoSelecionada?: OrdemServico;

  pagamento: any = {
    km: null, valorChamado: null, valorKm: null,
    pedagio: null, estacionamento: null, outros: '',
    valorOutros: null, lote: '', cpfNf: '',
    tipoPagamento: '', banco: '', urlComprovante: '',
    chavePix: '', pago: false
  };

  carregandoPagamento    = false;
  uploadandoComprovante  = false;
  pagamentoModoEdicao    = false;
  pagamentoJaExiste      = false;
  menuPagamentoDirecao: 'up' | 'down' = 'down';

  menuRecebimentoAberto?: string;
  menuRecebimentoPosicao = { top: 0, left: 0 };
  modalRecebimentoAberto = false;
  ordemRecebimentoSelecionada?: OrdemServico;
  carregandoRecebimento     = false;
  uploadandoComprovanteRec  = false;
  recebimentoModoEdicao     = false;
  recebimentoJaExiste       = false;

  recebimento: any = {
    km: null, valorChamado: null, valorKm: null,
    pedagio: null, estacionamento: null, outros: '',
    valorOutros: null, lote: '', nf: '',
    tipoPagamento: '', banco: '', urlComprovante: '',
    dataPrevista: null, dataPagamento: null, recebido: false
  };

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
      clientes: this.clienteService.listar().pipe(map(p => p.content ?? [])),
      credenciados: this.credenciadoService.listar().pipe(map(p => p.content ?? []))
    }).subscribe(({ clientes, credenciados }) => {
      this.clientes    = clientes;
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
    if (dataISO) filtroConvertido.dataAbertura = dataISO;
    else         delete filtroConvertido.dataAbertura;

    this.service.listar(this.page, this.size, filtroConvertido).subscribe({
      next: res => {
        this.ordensServico = (res.content ?? []).map(os => ({
          ...os,
          clienteNome:     os.cliente?.razaoSocial || os.cliente?.nome || '-',
          credenciadoNome: os.credenciado?.rag || '-'
        }));
        this.totalElements = res.totalElements ?? this.ordensServico.length;
        this.totalPages    = typeof res.totalPages === 'number'
          ? res.totalPages
          : Math.ceil(this.totalElements / this.size);
        this.loading = false;
      },
      error: err => {
        console.error('Erro ao carregar OS', err);
        this.errorMessage = 'Erro ao carregar Ordens de Serviço';
        this.loading = false;
      }
    });
  }

  carregarOrdens() { this.filtroSubject.next(); }

  excluir(id?: string) {
    if (!id) return alert('ID inválido para exclusão.');
    if (confirm('Tem certeza que deseja excluir esta OS?')) {
      this.service.excluir(id.toString()).subscribe({
        next: () => this.carregarOrdens(),
        error: () => alert('Erro ao excluir Ordem de Serviço.')
      });
    }
  }

  editar(id?: string)      { this.router.navigate(['/ordem-servico/editar', id]); }
  verHistorico(id?: string) { if (id) this.router.navigate(['/ordem-servico', id, 'historico']); }

  paginaAnterior() { if (this.page > 0) { this.page--; this._carregarOrdens(); } }
  proximaPagina()  { if ((this.page + 1) < this.totalPages) { this.page++; this._carregarOrdens(); } }

  onSizeChange(event: Event) {
    this.size = Number((event.target as HTMLSelectElement).value);
    this.page = 0;
    this._carregarOrdens();
  }

  abrirModalSolucao(id?: string) {
    if (!id) return;
    const os = this.ordensServico.find(o => o.id === id);
    if (!os) return;
    this.ordemSelecionada    = os;
    this.ordemSelecionadaId  = id;
    this.modalSolucaoAberto  = true;
    this.solucao = new Solucao();
  }

  fecharModalSolucao() {
    this.modalSolucaoAberto  = false;
    this.solucao = new Solucao();
    this.ordemSelecionada    = undefined;
    this.copiarAposFinalizarOS = false;
  }

  private toISO(dateTimeLocal?: string): string | undefined {
    if (!dateTimeLocal) return undefined;
    return new Date(dateTimeLocal).toISOString();
  }

  /** Converte OffsetDateTime/ISO → "yyyy-MM-dd" para input[type=date] */
  private toDateInput(value?: string | null): string | null {
    if (!value) return null;
    return value.substring(0, 10);
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
      horaFinal:   this.toISO(this.solucao.horaFinal)
    };

    const copiar        = this.copiarAposFinalizarOS;
    const ordemParaCopiar = { ...this.ordemSelecionada };

    this.service.finalizarOS(this.ordemSelecionadaId, payload).subscribe({
      next: () => {
        this.fecharModalSolucao();
        this.carregarOrdens();
        if (copiar) {
          this.router.navigate(['/ordem-servico/cadastro'], { state: { copiarDe: ordemParaCopiar } });
        } else {
          alert('OS finalizada com sucesso!');
        }
      },
      error: () => alert('Erro ao finalizar OS')
    });
  }

  abrirModalVisualizacao(ordemId: string) {
    this.solucaoService.buscarPorOrdem(ordemId).subscribe({
      next: solucao => { this.solucaoVisualizacao = solucao; this.modalVisualizarSolucao = true; },
      error: ()     => alert('Erro ao carregar solução.')
    });
  }

  fecharModalVisualizacao() {
    this.modalVisualizarSolucao = false;
    this.solucaoVisualizacao    = undefined;
  }

  acaoSolucao(os: OrdemServico) {
    if (os.status === 'CONCLUIDA') this.abrirModalVisualizacao(os.id!);
    else                           this.abrirModalSolucao(os.id);
  }

  exportar(formato: 'xlsx' | 'csv' | 'pdf') {
    const chamada$ = formato === 'xlsx' ? this.service.exportarXlsx(this.filtro)
                   : formato === 'csv'  ? this.service.exportarCsv(this.filtro)
                   :                      this.service.exportarPdf(this.filtro);

    chamada$.subscribe({
      next: (blob: Blob) => {
        const url  = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href  = url;
        link.download = `ordens-servico.${formato}`;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: () => alert(`Erro ao exportar ${formato.toUpperCase()}`)
    });
  }

  abrirModalEmail() {
    const s = this.solucaoVisualizacao;
    if (!s) return;

    const data        = s.dataAtendimento ? new Date(s.dataAtendimento).toLocaleDateString('pt-BR') : '';
    const horaInicial = s.horaInicial     ? new Date(s.horaInicial).toLocaleTimeString('pt-BR')     : '';
    const horaFinal   = s.horaFinal       ? new Date(s.horaFinal).toLocaleTimeString('pt-BR')       : '';

    const html = `
<table width="100%" cellpadding="0" cellspacing="0"
       style="font-family: Arial, sans-serif; font-size:14px; border-collapse: collapse;">
  <tr><td style="background-color:#8ea6c9; padding:8px; font-weight:bold; color:#000;">POSICIONAMENTO DE ATENDIMENTO:</td></tr>
  <tr><td style="padding:8px;"><strong>OS:</strong> ${s.osClt ?? ''}</td></tr>
  <tr><td style="padding:8px;"><strong>Data:</strong> ${data} &nbsp;&nbsp; das ${horaInicial} &nbsp;&nbsp; às ${horaFinal}.</td></tr>
  <tr><td style="padding:8px;"><strong>Solução:</strong> ${s.solucao ?? ''}</td></tr>
</table>`;

    this.textoEmailFormatado = this.sanitizer.bypassSecurityTrustHtml(html);
    this.modalEmail = true;
  }

  async copiarTexto(element: HTMLElement) {
    try {
      const blob = new Blob([element.innerHTML], { type: 'text/html' });
      await navigator.clipboard.write([new ClipboardItem({ 'text/html': blob })]);
      alert('Texto formatado copiado com sucesso!');
    } catch {
      await navigator.clipboard.writeText(element.innerText);
      alert('Texto copiado (modo simples).');
    }
  }

  private converterDataParaISO(data?: string): string | undefined {
    if (!data) return undefined;
    const d = data.replace(/\//g, '');
    if (d.length !== 8 || isNaN(Number(d))) return undefined;
    return `${d.substring(4, 8)}-${d.substring(2, 4)}-${d.substring(0, 2)}`;
  }

  abrirModalRastreio(os: OrdemServico) {
    if (!os.rastreio) return;
    this.ordemRastreioSelecionada    = os;
    this.statusRastreioSelecionado   = os.statusRastreio;
    this.modalRastreioAberto         = true;
  }

  fecharModalRastreio() {
    this.modalRastreioAberto       = false;
    this.ordemRastreioSelecionada  = undefined;
    this.statusRastreioSelecionado = undefined;
  }

  salvarStatusRastreio() {
    const id = this.ordemRastreioSelecionada?.id;
    if (!id || !this.statusRastreioSelecionado) return;
    this.service.atualizarStatusRastreio(id, this.statusRastreioSelecionado).subscribe({
      next: () => { this.fecharModalRastreio(); this.carregarOrdens(); },
      error: ()  => alert('Erro ao atualizar status do rastreio.')
    });
  }

  getCorRastreio(statusRastreio?: string): string {
    return this.statusRastreioOpcoes.find(s => s.value === statusRastreio)?.cor ?? '#9CA3AF';
  }

  imprimirOS(os: OrdemServico, formato: 'pdf' | 'xlsx') {
    const chamada$ = formato === 'pdf'
      ? this.service.relatorioIndividualPdf(os.id!)
      : this.service.relatorioIndividualXlsx(os.id!);

    chamada$.subscribe({
      next: (blob: Blob) => {
        const url  = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href  = url;
        link.download = `OS-${os.osClt ?? os.id}.${formato}`;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: () => alert('Erro ao gerar relatório da OS.')
    });
  }

  toggleExportMenuOS(id: string) {
    this.exportMenuOSAberto = this.exportMenuOSAberto === id ? undefined : id;
  }
  fecharExportMenuOS() { this.exportMenuOSAberto = undefined; }

  // ── PAGAMENTO AO CREDENCIADO (Contas a Pagar) ────────────────────────────

  togglePagamentoMenu(os: OrdemServico, event: MouseEvent) {
    if (this.menuPagamentoAberto === os.id) { this.menuPagamentoAberto = undefined; return; }
    const b = (event.currentTarget as HTMLElement).getBoundingClientRect();
    const top = (window.innerHeight - b.bottom) < 80 ? b.top - 80 : b.bottom + 4;
    this.menuPagamentoPosicao = { top, left: b.right - 176 };
    this.menuPagamentoAberto  = os.id;
  }
  fecharMenuPagamento() { this.menuPagamentoAberto = undefined; }

  abrirModalPagamento(os: OrdemServico) {
    this.ordemPagamentoSelecionada = os;
    this.pagamento = {
      km: null, valorChamado: null, valorKm: null,
      pedagio: null, estacionamento: null, outros: '',
      valorOutros: null, lote: '', cpfNf: '',
      tipoPagamento: '', banco: '', urlComprovante: '',
      chavePix: '', pago: false
    };
    this.pagamentoJaExiste    = false;
    this.carregandoPagamento  = true;
    this.modalPagamentoAberto = true;

    const credenciadoId = os.credenciado?.id;
    if (credenciadoId) {
      this.credenciadoService.buscarPorId(credenciadoId).subscribe({
        next: (c) => {
          if (c.valorChamado != null)    this.pagamento.valorChamado = c.valorChamado;
          if (c.valorKm != null)         this.pagamento.valorKm      = c.valorKm;
          if (c.tipoFluxoPagamento)      this.pagamento.lote         = c.tipoFluxoPagamento;
          if (c.chavePix)                this.pagamento.chavePix     = c.chavePix;
        },
        error: () => {}
      });
    }

    forkJoin({
      pagamento: this.service.buscarPagamento(os.id!).pipe(catchError(() => of(null))),
      solucao:   this.solucaoService.buscarPorOrdem(os.id!).pipe(catchError(() => of(null)))
    }).subscribe({
      next: ({ pagamento: pag, solucao }) => {
        if (!solucao && !pag) {
          this.carregandoPagamento  = false;
          this.modalPagamentoAberto = false;
          alert('Esta OS ainda não possui solução registrada. Finalize a OS antes de registrar o pagamento.');
          return;
        }
        if (pag && pag.id != null && pag.pago === true) {
          this.pagamento = {
            km: pag.km, valorChamado: pag.valorChamado, valorKm: pag.valorKm,
            pedagio: pag.pedagio, estacionamento: pag.estacionamento,
            outros: pag.outros ?? '', valorOutros: pag.valorOutros,
            lote: pag.lote ?? '', cpfNf: pag.cpfNf ?? '',
            tipoPagamento: pag.tipoPagamento ?? '', banco: pag.banco ?? '',
            urlComprovante: pag.urlComprovante ?? '',
            chavePix: pag.chavePix ?? this.pagamento.chavePix ?? '',
            pago: true
          };
          this.pagamentoJaExiste   = true;
          this.pagamentoModoEdicao = false;
        } else {
          if (pag && pag.id != null) {
            this.pagamento.km             = pag.km;
            this.pagamento.valorChamado   = pag.valorChamado;
            this.pagamento.valorKm        = pag.valorKm;
            this.pagamento.pedagio        = pag.pedagio;
            this.pagamento.estacionamento = pag.estacionamento;
            this.pagamento.outros         = pag.outros ?? '';
            this.pagamento.valorOutros    = pag.valorOutros;
            this.pagamento.lote           = pag.lote ?? '';
            this.pagamento.cpfNf          = pag.cpfNf ?? '';
            this.pagamento.tipoPagamento  = pag.tipoPagamento ?? '';
            this.pagamento.banco          = pag.banco ?? '';
            this.pagamento.urlComprovante = pag.urlComprovante ?? '';
            this.pagamento.chavePix       = pag.chavePix ?? this.pagamento.chavePix ?? '';
            this.pagamento.pago           = false;
          } else if (solucao) {
            this.pagamento.km             = solucao.km;
            this.pagamento.pedagio        = solucao.pedagio;
            this.pagamento.estacionamento = solucao.estacionamento;
            this.pagamento.valorOutros    = solucao.outros;
          }
          this.pagamentoJaExiste   = false;
          this.pagamentoModoEdicao = true;
        }
        this.carregandoPagamento = false;
      },
      error: () => { this.carregandoPagamento = false; }
    });
  }

  fecharModalPagamento() {
    this.modalPagamentoAberto  = false;
    this.pagamento             = {};
    this.pagamentoModoEdicao   = false;
    this.pagamentoJaExiste     = false;
  }

  salvarPagamento() {
    if (!this.ordemPagamentoSelecionada?.id) return;
    if (!this.pagamento.tipoPagamento) { alert('Selecione o tipo de pagamento.'); return; }

    const payload = {
      tipoPagamento: this.pagamento.tipoPagamento, lote: this.pagamento.lote,
      cpfNf: this.pagamento.cpfNf, banco: this.pagamento.banco,
      urlComprovante: this.pagamento.urlComprovante,
      valorChamado: this.toNumber(this.pagamento.valorChamado),
      km: this.toNumber(this.pagamento.km), valorKm: this.toNumber(this.pagamento.valorKm),
      pedagio: this.toNumber(this.pagamento.pedagio),
      estacionamento: this.toNumber(this.pagamento.estacionamento),
      valorOutros: this.toNumber(this.pagamento.valorOutros),
      outros: this.pagamento.outros, chavePix: this.pagamento.chavePix
    };

    const chamada$ = this.pagamentoJaExiste
      ? this.service.editarPagamento(this.ordemPagamentoSelecionada.id!, payload)
      : this.service.registrar(this.ordemPagamentoSelecionada.id!, payload);

    chamada$.subscribe({
      next: () => {
        alert('Pagamento salvo com sucesso!');
        this.pagamentoModoEdicao = false;
        this.fecharModalPagamento();
        this.carregarOrdens();
      },
      error: () => alert('Erro ao salvar pagamento.')
    });
  }

  toNumber(value: any): number {
    if (value === null || value === undefined || value === '') return 0;
    return Number(value);
  }

  onTipoPagamentoChange() {
    if (this.pagamento.tipoPagamento !== 'PIX') this.pagamento.chavePix = '';
  }

  habilitarEdicaoPagamento() { this.pagamentoModoEdicao = true; }

  calcularTotalPreview(): number {
    const p = this.pagamento;
    return (p.valorChamado || 0) + ((p.km || 0) * (p.valorKm || 0))
         + (p.pedagio || 0) + (p.estacionamento || 0) + (p.valorOutros || 0);
  }

  calcularTotalKm(): number {
    return (this.pagamento?.km || 0) * (this.pagamento?.valorKm || 0);
  }

  async uploadComprovante(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    const osg = this.ordemPagamentoSelecionada?.osg;
    if (!osg) { alert('OSG não encontrada para nomear o comprovante.'); return; }
    this.uploadandoComprovante = true;
    this.service.uploadComprovante(file, osg).subscribe({
      next: (url) => { this.pagamento.urlComprovante = url; this.uploadandoComprovante = false; },
      error: ()   => { alert('Erro ao fazer upload do comprovante.'); this.uploadandoComprovante = false; }
    });
  }

  removerComprovante() {
    const url = this.pagamento.urlComprovante;
    if (!url) return;
    this.service.deletarComprovante(url).subscribe({
      next: () => { this.pagamento.urlComprovante = ''; },
      error: () => { this.pagamento.urlComprovante = ''; }
    });
  }

  async downloadComprovante() {
    if (this.pagamento.urlComprovante) window.open(this.pagamento.urlComprovante, '_blank');
  }

  getOrdemById(id: string): OrdemServico {
    return this.ordensServico.find(o => o.id === id)!;
  }

  // ── RECEBIMENTO DO CLIENTE (Contas a Receber) ────────────────────────────

  toggleRecebimentoMenu(os: OrdemServico, event: MouseEvent) {
    if (this.menuRecebimentoAberto === os.id) { this.menuRecebimentoAberto = undefined; return; }
    const b = (event.currentTarget as HTMLElement).getBoundingClientRect();
    const top = (window.innerHeight - b.bottom) < 80 ? b.top - 80 : b.bottom + 4;
    this.menuRecebimentoPosicao = { top, left: b.right - 176 };
    this.menuRecebimentoAberto  = os.id;
  }
  fecharMenuRecebimento() { this.menuRecebimentoAberto = undefined; }

  /**
   * Gera o nome automático do lote baseado no tipoFluxoPagamento do cliente.
   *
   * MENSAL    → CPA_ABR2026
   * QUINZENAL → CPA_ABR2026A (dias 1-15) | CPA_ABR2026B (dias 16-31)
   * SEMANAL   → CPA_ABR2026S1 | S2 | S3 | S4
   * UNICO     → CPA_ABR2026
   */
  private gerarNomeLote(tipoFluxo: string): string {
    const meses = ['JAN','FEV','MAR','ABR','MAI','JUN','JUL','AGO','SET','OUT','NOV','DEZ'];
    const hoje  = new Date();
    const mes   = meses[hoje.getMonth()];
    const ano   = hoje.getFullYear();
    const base  = `CPA_${mes}${ano}`;
    const dia   = hoje.getDate();

    switch (tipoFluxo) {
      case 'QUINZENAL':
        return base + (dia <= 15 ? 'A' : 'B');
      case 'SEMANAL': {
        // Semana do mês: 1–7 → S1, 8–14 → S2, 15–21 → S3, 22+ → S4
        const semana = dia <= 7 ? 1 : dia <= 14 ? 2 : dia <= 21 ? 3 : 4;
        return `${base}S${semana}`;
      }
      case 'MENSAL':
      case 'UNICO':
      default:
        return base;
    }
  }

  abrirModalRecebimento(os: OrdemServico) {
    this.ordemRecebimentoSelecionada = os;
    this.recebimento = {
      km: null, valorChamado: null, valorKm: null,
      pedagio: null, estacionamento: null, outros: '',
      valorOutros: null, lote: '', nf: '',
      tipoPagamento: '', banco: '', urlComprovante: '',
      dataPrevista: null, dataPagamento: null, recebido: false
    };
    this.recebimentoJaExiste    = false;
    this.carregandoRecebimento  = true;
    this.modalRecebimentoAberto = true;

    // 1️⃣ Pré-preenche com valores padrão do cliente + lote automático
    const clienteId = os.cliente?.id;
    if (clienteId) {
      this.clienteService.buscarPorId(clienteId).subscribe({
        next: (cliente) => {
          if (cliente.valorChamado != null) this.recebimento.valorChamado = cliente.valorChamado;
          if (cliente.valorKm != null)      this.recebimento.valorKm      = cliente.valorKm;
          if (cliente.tipoFluxoPagamento)   this.recebimento.lote = this.gerarNomeLote(cliente.tipoFluxoPagamento);
        },
        error: () => {}
      });
    }

    // 2️⃣ Recebimento existente + Solução em paralelo
    forkJoin({
      recebimento: this.service.buscarRecebimento(os.id!).pipe(catchError(() => of(null))),
      solucao:     this.solucaoService.buscarPorOrdem(os.id!).pipe(catchError(() => of(null)))
    }).subscribe({
      next: ({ recebimento: rec, solucao }) => {

        if (!solucao && !rec) {
          this.carregandoRecebimento  = false;
          this.modalRecebimentoAberto = false;
          alert('Esta OS ainda não possui solução registrada. Finalize a OS antes de registrar o recebimento.');
          return;
        }

        if (rec && rec.id != null && rec.recebido === true) {
          // ── Modo leitura ──
          this.recebimento = {
            km: rec.km, valorChamado: rec.valorChamado, valorKm: rec.valorKm,
            pedagio: rec.pedagio, estacionamento: rec.estacionamento,
            outros: rec.outros ?? '', valorOutros: rec.valorOutros,
            lote: rec.lote ?? '', nf: rec.nf ?? '',
            tipoPagamento: rec.tipoPagamento ?? '', banco: rec.banco ?? '',
            urlComprovante: rec.urlComprovante ?? '',
            dataPrevista:  this.toDateInput(rec.dataPrevista),
            dataPagamento: this.toDateInput(rec.dataPagamento),
            recebido: true
          };
          this.recebimentoJaExiste   = true;
          this.recebimentoModoEdicao = false;

        } else {
          // ── Modo edição ──
          if (rec && rec.id != null) {
            this.recebimento.km             = rec.km;
            this.recebimento.valorChamado   = rec.valorChamado;
            this.recebimento.valorKm        = rec.valorKm;
            this.recebimento.pedagio        = rec.pedagio;
            this.recebimento.estacionamento = rec.estacionamento;
            this.recebimento.outros         = rec.outros ?? '';
            this.recebimento.valorOutros    = rec.valorOutros;
            this.recebimento.lote           = rec.lote ?? '';
            this.recebimento.nf             = rec.nf ?? '';
            this.recebimento.tipoPagamento  = rec.tipoPagamento ?? '';
            this.recebimento.banco          = rec.banco ?? '';
            this.recebimento.urlComprovante = rec.urlComprovante ?? '';
            this.recebimento.dataPrevista   = this.toDateInput(rec.dataPrevista);
            this.recebimento.dataPagamento  = this.toDateInput(rec.dataPagamento);
            this.recebimento.recebido       = false;
          } else if (solucao) {
            this.recebimento.km             = solucao.km;
            this.recebimento.pedagio        = solucao.pedagio;
            this.recebimento.estacionamento = solucao.estacionamento;
            this.recebimento.valorOutros    = solucao.outros;
          }
          this.recebimentoJaExiste   = false;
          this.recebimentoModoEdicao = true;
        }

        this.carregandoRecebimento = false;
      },
      error: () => { this.carregandoRecebimento = false; }
    });
  }

  fecharModalRecebimento() {
    this.modalRecebimentoAberto  = false;
    this.recebimento             = {};
    this.recebimentoModoEdicao   = false;
    this.recebimentoJaExiste     = false;
  }

  salvarRecebimento() {
    if (!this.ordemRecebimentoSelecionada?.id) return;
    if (!this.recebimento.tipoPagamento) { alert('Selecione o tipo de pagamento.'); return; }

    const payload = {
      tipoPagamento:  this.recebimento.tipoPagamento,
      lote:           this.recebimento.lote,
      nf:             this.recebimento.nf,
      banco:          this.recebimento.banco,
      urlComprovante: this.recebimento.urlComprovante,
      valorChamado:   this.toNumber(this.recebimento.valorChamado),
      km:             this.toNumber(this.recebimento.km),
      valorKm:        this.toNumber(this.recebimento.valorKm),
      pedagio:        this.toNumber(this.recebimento.pedagio),
      estacionamento: this.toNumber(this.recebimento.estacionamento),
      valorOutros:    this.toNumber(this.recebimento.valorOutros),
      outros:         this.recebimento.outros,
      dataPrevista:   this.recebimento.dataPrevista  || null,
      dataPagamento:  this.recebimento.dataPagamento || null
    };

    const chamada$ = this.recebimentoJaExiste
      ? this.service.editarRecebimento(this.ordemRecebimentoSelecionada.id!, payload)
      : this.service.registrarRecebimento(this.ordemRecebimentoSelecionada.id!, payload);

    chamada$.subscribe({
      next: () => {
        alert('Recebimento salvo com sucesso!');
        this.recebimentoModoEdicao = false;
        this.fecharModalRecebimento();
        this.carregarOrdens();
      },
      error: () => alert('Erro ao salvar recebimento.')
    });
  }

  habilitarEdicaoRecebimento() { this.recebimentoModoEdicao = true; }

  calcularTotalPreviewRecebimento(): number {
    const r = this.recebimento;
    return (r.valorChamado || 0) + ((r.km || 0) * (r.valorKm || 0))
         + (r.pedagio || 0) + (r.estacionamento || 0) + (r.valorOutros || 0);
  }

  calcularTotalKmRecebimento(): number {
    return (this.recebimento?.km || 0) * (this.recebimento?.valorKm || 0);
  }

  /** Upload comprovante de recebimento → pasta recebimentos/ */
  async uploadComprovanteRecebimento(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;
    const osg = this.ordemRecebimentoSelecionada?.osg;
    if (!osg) { alert('OSG não encontrada para nomear o comprovante.'); return; }
    this.uploadandoComprovanteRec = true;
    this.service.uploadComprovanteRecebimento(file, osg).subscribe({
      next: (url) => { this.recebimento.urlComprovante = url; this.uploadandoComprovanteRec = false; },
      error: ()   => { alert('Erro ao fazer upload do comprovante.'); this.uploadandoComprovanteRec = false; }
    });
  }

  removerComprovanteRecebimento() {
    const url = this.recebimento.urlComprovante;
    if (!url) return;
    this.service.deletarComprovanteRecebimento(url).subscribe({
      next: () => { this.recebimento.urlComprovante = ''; },
      error: () => { this.recebimento.urlComprovante = ''; }
    });
  }

  async downloadComprovanteRecebimento() {
    if (this.recebimento.urlComprovante) window.open(this.recebimento.urlComprovante, '_blank');
  }
}