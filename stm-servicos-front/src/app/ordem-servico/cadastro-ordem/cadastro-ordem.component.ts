import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { OrdemServicoService } from '../ordem-servico.service';
import { ActivatedRoute, Router } from '@angular/router';
import { OrdemServico } from '../ordem-servico';
import { ClienteService } from '../../clientes/cliente.service';
import { BehaviorSubject, catchError, forkJoin, map, of } from 'rxjs';
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
  private cepCliente: string | null = null;

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
      dataHoraAbertura: new FormControl<string | null>(null),

      clienteId: new FormControl<string | null>(null),
      codigoCliente: new FormControl<string | null>(null),
      nomeCliente: new FormControl<string | null>({ value: '', disabled: true }),

      credenciadoId: new FormControl<string | null>(null),
      codigoCredenciado: new FormControl<string | null>(null),
      nomeCredenciado: new FormControl<string | null>({ value: '', disabled: true }),
      tecnicoId: new FormControl<string | null>(null),

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

      raioKm: new FormControl(100),

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
      const now = new Date();

      const isoLocal = new Date(
        now.getTime() - now.getTimezoneOffset() * 60000
      ).toISOString().slice(0, 16);

      this.camposForm.patchValue({
        dataHoraAbertura: isoLocal
      });

      this.service.buscarProximoOsg().subscribe({
        next: osg => this.camposForm.patchValue({ osg, status: 'ABERTA' }),
        error: err => console.error('Erro ao buscar próximo OSG:', err)
      });

      return;
    }

    this.service.buscarPorId(idStr).subscribe({
      next: os => {
        // Observables de contratos e credenciados
        const contratos$ = os.cliente?.id
          ? this.clienteService.buscarPorId(os.cliente.id).pipe(
            map(cliente => cliente.contratos ?? []),
            catchError(() => of([]))
          )
          : of([]);

        console.log('OS', os)

        const credenciados$ = os.cliente?.endereco?.cep
          ? this.credenciadoService.buscarProximosPorCep(os.cliente.endereco.cep)
            .pipe(catchError(() => of([] as Credenciado[])))
          : of([] as Credenciado[]);

        forkJoin([contratos$, credenciados$]).subscribe(([contratos, credenciados]) => {

          // 1️⃣ Atualiza contratos ANTES
          this.contratosCliente$.next(contratos);

          // 2️⃣ Atualiza credenciados
          const listaCredenciados = [...credenciados];
          if (os.credenciado && !listaCredenciados.find(c => c.id === os.credenciado?.id)) {
            listaCredenciados.push({
              id: os.credenciado.id,
              rag: os.credenciado.rag,
              codigo: os.credenciado.codigo
            });
          }

          console.log('Contrato salvo:', os.contrato?.id?.toString());
          console.log(
            'Contratos disponíveis:',
            contratos.map(c => c.id!.toString())
          );

          this.credenciadosProximos$.next(listaCredenciados);

          // 3️⃣ Patch geral (SEM contrato e técnico)
          this.camposForm.patchValue({
            id: os.id,
            osClt: os.osClt,
            osg: os.osg,
            status: os.status,
            dataHoraAbertura: os.dataHoraAbertura
              ? os.dataHoraAbertura.substring(0, 16)
              : null,

            clienteId: os.cliente?.id || null,
            codigoCliente: os.cliente?.codigo || null,
            nomeCliente: os.cliente?.razaoSocial || os.cliente?.nome || '',

            credenciadoId: os.credenciado?.id || null,

            contato: os.contato,
            departamento: os.departamento,
            telefone: os.telefone,

            logradouro: os.endereco?.logradouro,
            numero: os.endereco?.numero,
            bairro: os.endereco?.bairro,
            cidade: os.endereco?.cidade,
            estado: os.endereco?.estado,
            complemento: os.endereco?.complemento,
            cep: os.endereco?.cep,

            acionador: os.acionador,
            equipamento: os.equipamento,
            serie: os.serie,
            pib: os.pib,
            defeito: os.defeito,
            rastreio: os.rastreio
          });

          // 4️⃣ Agora SIM seta contrato (lista já existe)
          if (os.contrato?.id) {
            this.camposForm.get('contratoId')?.setValue(os.contrato.id.toString());
          }

          // 5️⃣ Carrega técnicos e seta técnico APÓS carregar
          if (os.credenciado?.id) {
            this.buscarTecnicosDoCredenciado(os.credenciado.id, os.tecnico?.id);
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
          nomeCliente: cliente.razaoSocial || cliente.nome
        });

        this.contratosCliente$.next(cliente.contratos ?? []);

        if (cliente.endereco?.cep) {
          this.cepCliente = cliente.endereco?.cep;

          const raioKm = this.camposForm.get('raioKm')?.value ?? 100;

          this.credenciadoService
            .buscarProximosPorCep(this.cepCliente, raioKm)
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
    this.camposForm.patchValue({
      credenciadoId: null,
      tecnicoId: null,
      nomeCredenciado: ''
    });
    this.tecnicosCredenciado = [];
  }

  private montarObjeto(): OrdemServico {
    const f = this.camposForm.getRawValue();
    return {
      id: f.id,
      osClt: f.osClt,
      osg: f.osg,
      status: f.status,
      dataHoraAbertura: f.dataHoraAbertura
        ? new Date(f.dataHoraAbertura).toISOString()
        : undefined,
      clienteId: f.clienteId,
      credenciadoId: f.credenciadoId,
      tecnicoId: f.tecnicoId,
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
    const credenciadoId = event.target.value;

    this.camposForm.patchValue({
      credenciadoId,
      tecnicoId: null // reseta técnico
    });

    this.tecnicosCredenciado = [];

    if (credenciadoId) {
      this.buscarTecnicosDoCredenciado(credenciadoId);
    }
  }

  // 🔥 ALTERAÇÃO: aceita tecnicoId opcional para edição
  buscarTecnicosDoCredenciado(credenciadoId: string, tecnicoId?: string) {
    this.credenciadoService
      .listarTecnicos(credenciadoId, 0, 50)
      .subscribe({
        next: page => {
          this.tecnicosCredenciado = page.content;

          // 🔥 AGORA o select já tem options → setValue funciona
          if (tecnicoId) {
            this.camposForm.get('tecnicoId')?.setValue(tecnicoId);
          }
        },
        error: () => this.tecnicosCredenciado = []
      });
  }

  rebuscarCredenciados() {
    this.buscarCredenciadosPorCepFormulario();
  }

  buscarCredenciadosPorCepFormulario() {
    const cep = this.camposForm.get('cep')?.value;
    const raioKm = this.camposForm.get('raioKm')?.value ?? 100;

    if (!cep || cep.replace(/\D/g, '').length !== 8) {
      this.credenciadosProximos$.next([]);
      return;
    }

    this.credenciadoService
      .buscarProximosPorCep(cep, raioKm)
      .subscribe({
        next: c => this.credenciadosProximos$.next(c),
        error: () => this.credenciadosProximos$.next([])
      });
  }

  compareContrato = (a: string | null, b: string | null): boolean => {
    return a === b;
  };
}