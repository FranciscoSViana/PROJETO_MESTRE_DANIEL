import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { OrdemServicoService } from '../ordem-servico.service';
import { ActivatedRoute, Router } from '@angular/router';
import { OrdemServico } from '../ordem-servico';
import { ClienteService } from '../../clientes/cliente.service';
import { BehaviorSubject, catchError, forkJoin, of } from 'rxjs';
import { CredenciadoService } from '../../credenciados/credenciado.service';
import { Cliente } from '../../clientes/cliente';
import { Credenciado } from '../../credenciados/credenciado';

@Component({
  selector: 'app-cadastro-ordem',
  standalone: false,
  templateUrl: './cadastro-ordem.component.html',
  styleUrl: './cadastro-ordem.component.scss'
})
export class CadastroOrdemComponent implements OnInit {

  camposForm: FormGroup;

  // Agora reativos
  contratosCliente$ = new BehaviorSubject<any[]>([]);
  credenciadosProximos$ = new BehaviorSubject<Credenciado[]>([]);
  tecnicosCredenciado: any[] = [];

  constructor(
    private service: OrdemServicoService,
    private clienteService: ClienteService,
    private credenciadoService: CredenciadoService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.camposForm = new FormGroup({
      id: new FormControl<number | null>(null),
      osClt: new FormControl<string | null>(null),
      osg: new FormControl<string | null>({ value: '', disabled: true }),
      status: new FormControl<string>({ value: 'ABERTA', disabled: true }),
      rag: new FormControl<string | null>(null),
      dataHora: new FormControl<string | null>(null),

      clienteId: new FormControl<string | null>(null),
      codigoCliente: new FormControl<string | null>(null),
      nomeCliente: new FormControl<string | null>({ value: '', disabled: true }),

      credenciadoId: new FormControl<string | null>(null),
      codigoCredenciado: new FormControl<string | null>(null),
      nomeCredenciado: new FormControl<string | null>({ value: '', disabled: true }),

      contratoId: new FormControl<string | null>(null, Validators.required),
      contato: new FormControl<string | null>(null),
      departamento: new FormControl<string | null>(null),
      telefone: new FormControl<string | null>(null),

      logradouro: new FormControl<string | null>(null),
      numero: new FormControl<string | null>(null),
      bairro: new FormControl<string | null>(null),
      cidade: new FormControl<string | null>(null),
      estado: new FormControl<string | null>(null),
      complemento: new FormControl<string | null>(null),
      cep: new FormControl<string | null>(null),

      acionador: new FormControl<string | null>(null),
      equipamento: new FormControl<string | null>(null),
      serie: new FormControl<string | null>(null),
      pib: new FormControl<string | null>(null),
      defeito: new FormControl<string | null>(null),
      rastreio: new FormControl<string | null>(null),
    });
  }

  ngOnInit(): void {
    const idStr = this.route.snapshot.paramMap.get('id');

    if (!idStr) {
      this.service.buscarProximoOsg().subscribe({
        next: osg => this.camposForm.patchValue({ osg, status: 'ABERTA' }),
        error: err => console.error('Erro ao buscar próximo OSG:', err)
      });
      return;
    }

    this.service.buscarPorId(idStr).subscribe({
      next: os => {
        // Observables de contratos e credenciados
        const contratos$ = of(os.cliente?.contratos ?? []);
        const credenciados$ = os.cliente?.endereco?.cep
          ? this.credenciadoService.buscarProximosPorCep(os.cliente.endereco.cep)
            .pipe(catchError(() => of([] as Credenciado[])))
          : of([] as Credenciado[]);

        forkJoin([contratos$, credenciados$]).subscribe(([contratos, credenciados]) => {
          // Atualiza BehaviorSubjects
          this.contratosCliente$.next(contratos);

          const listaCredenciados = [...credenciados];
          if (os.credenciado && !listaCredenciados.find(c => c.id === os.credenciado?.id)) {
            listaCredenciados.push({
              id: os.credenciado.id,
              rag: os.credenciado.rag,
              codigo: os.credenciado.codigo
            });
          }
          this.credenciadosProximos$.next(listaCredenciados);

          // Patch no formulário
          this.camposForm.patchValue({
            id: os.id,
            osClt: os.osClt,
            osg: os.osg,
            status: os.status,
            dataHora: os.dataHora,
            clienteId: os.cliente?.id || null,
            codigoCliente: os.cliente?.codigo || null,
            nomeCliente: os.cliente?.razaoSocial || os.cliente?.nome || '',
            credenciadoId: os.credenciado?.id || null,
            codigoCredenciado: os.credenciado?.codigo || null,
            nomeCredenciado: os.credenciado?.rag || '',
            contratoId: os.contrato?.id || null,
            contato: os.contato,
            departamento: os.departamento,
            telefone: os.telefone,
            logradouro: os.cliente?.endereco?.logradouro,
            numero: os.cliente?.endereco?.numero,
            bairro: os.cliente?.endereco?.bairro,
            cidade: os.cliente?.endereco?.cidade,
            estado: os.cliente?.endereco?.estado,
            complemento: os.cliente?.endereco?.complemento,
            cep: os.cliente?.endereco?.cep,
            acionador: os.acionador,
            equipamento: os.equipamento,
            serie: os.serie,
            pib: os.pib,
            defeito: os.defeito,
            rastreio: os.rastreio
          });

          if (os.credenciado?.id) {
            this.buscarTecnicosDoCredenciado(os.credenciado.id);
          }
        });
      },
      error: err => console.error('Erro ao buscar OS:', err)
    });
  }

  salvar() {
    this.camposForm.markAllAsTouched();
    const os = this.montarObjeto();
    const acao$ = os.id ? this.service.atualizar(os.id, os) : this.service.salvar(os);

    acao$.subscribe({
      next: () => this.router.navigate(['/ordem-servico']),
      error: err => console.error('Erro ao salvar OS:', err)
    });
  }

  buscarClientePorCodigo() {
    const codigo = this.camposForm.get('codigoCliente')?.value;
    if (!codigo) { this.resetCliente(); return; }

    this.clienteService.buscarPorCodigo(codigo)
      .pipe(catchError(() => { this.resetCliente(); return of(null); }))
      .subscribe((cliente: Cliente | null) => {
        if (!cliente) return;

        this.camposForm.patchValue({
          clienteId: cliente.id,
          nomeCliente: cliente.razaoSocial || cliente.nome,
          logradouro: cliente.endereco?.logradouro,
          numero: cliente.endereco?.numero,
          bairro: cliente.endereco?.bairro,
          cidade: cliente.endereco?.cidade,
          estado: cliente.endereco?.estado,
          cep: cliente.endereco?.cep,
          complemento: cliente.endereco?.complemento
        });

        this.contratosCliente$.next(cliente.contratos ?? []);

        if (cliente.endereco?.cep) {
          this.credenciadoService.buscarProximosPorCep(cliente.endereco.cep)
            .subscribe(c => this.credenciadosProximos$.next(c));
        }
      });
  }

  buscarCredenciadoPorCodigo() {
    const codigo = this.camposForm.get('codigoCredenciado')?.value;
    if (!codigo) { this.resetCredenciado(); return; }

    this.credenciadoService.buscarPorCodigo(codigo)
      .pipe(catchError(() => { this.resetCredenciado(); return of(null); }))
      .subscribe(c => {
        if (!c) return;

        this.camposForm.patchValue({
          credenciadoId: c.id,
          nomeCredenciado: c.rag
        });
        this.buscarTecnicosDoCredenciado(c.id!);
      });
  }

  private resetCliente() {
    this.camposForm.patchValue({
      nomeCliente: '', clienteId: null,
      logradouro: '', numero: '', bairro: '', cidade: '', estado: '', cep: '', complemento: ''
    });
    this.contratosCliente$.next([]);
    this.credenciadosProximos$.next([]);
  }

  private resetCredenciado() {
    this.camposForm.patchValue({ nomeCredenciado: '', credenciadoId: null });
    this.tecnicosCredenciado = [];
  }

  private montarObjeto(): OrdemServico {
    const f = this.camposForm.getRawValue();
    return {
      id: f.id,
      osClt: f.osClt,
      osg: f.osg,
      status: f.status,
      dataHora: f.dataHora,
      clienteId: f.clienteId,
      credenciadoId: f.credenciadoId,
      contrato: f.contratoId,
      contato: f.contato,
      departamento: f.departamento,
      telefone: f.telefone,
      endereco: {
        logradouro: f.logradouro,
        numero: f.numero,
        bairro: f.bairro,
        cidade: f.cidade,
        estado: f.estado,
        cep: f.cep,
        complemento: f.complemento
      },
      acionador: f.acionador,
      equipamento: f.equipamento,
      serie: f.serie,
      pib: f.pib,
      defeito: f.defeito,
      rastreio: f.rastreio
    };
  }

  buscarCep() {
    const cep = this.camposForm.get('cep')?.value;
    if (!cep || cep.length < 8) return;

    this.service.buscarCep(cep).subscribe({
      next: d => this.camposForm.patchValue({
        logradouro: d.logradouro,
        bairro: d.bairro,
        cidade: d.localidade,
        estado: d.uf,
        complemento: d.complemento
      }),
      error: () => console.error("CEP não encontrado")
    });
  }

  onCredenciadoChange(event: any) {
    const id = event.target.value;
    if (!id) return;
    this.camposForm.patchValue({ credenciadoId: id });
    this.buscarTecnicosDoCredenciado(id);
  }

  buscarTecnicosDoCredenciado(id: string) {
    this.credenciadoService.listarTecnicos(id).subscribe(page => {
      this.tecnicosCredenciado = page.content;
    });
  }
}