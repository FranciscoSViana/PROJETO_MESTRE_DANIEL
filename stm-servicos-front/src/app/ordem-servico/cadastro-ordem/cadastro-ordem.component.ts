import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { OrdemServicoService } from '../ordem-servico.service';
import { ActivatedRoute, Router } from '@angular/router';
import { OrdemServico } from '../ordem-servico';
import { ClienteService } from '../../clientes/cliente.service';
import { catchError, of } from 'rxjs';
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

      contrato: new FormControl<string | null>(null),
      contato: new FormControl<string | null>(null),
      departamento: new FormControl<string | null>(null),
      telefone: new FormControl<string | null>(null),

      // Endereço
      logradouro: new FormControl<string | null>(null),
      numero: new FormControl<string | null>(null),
      bairro: new FormControl<string | null>(null),
      cidade: new FormControl<string | null>(null),
      estado: new FormControl<string | null>(null),
      complemento: new FormControl<string | null>(null),
      cep: new FormControl<string | null>(null),

      // Dados da OS
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

    // 👉 Se NÃO tiver ID → é NOVA OS → gerar OSG automaticamente
    if (!idStr) {
      this.service.buscarProximoOsg().subscribe(osg => {
        this.camposForm.patchValue({ 
          osg, 
          status: 'ABERTA'
        });
      });
      return;
    }

    this.camposForm.valueChanges.subscribe(v => console.log("FORM:", v));

    // 👉 Se tiver ID → é edição → carregar a OS existente
    const id = Number(idStr);
    this.service.buscarPorId(idStr).subscribe(os => {
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

        contrato: os.contrato,
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
    });
  }

  salvar() {
    this.camposForm.markAllAsTouched();

    const id = this.camposForm.get('id')?.value;

    const os: OrdemServico = this.montarObjeto();

    if (id) {
      // EDITAR
      this.service.atualizar(id, os).subscribe({
        next: () => this.router.navigate(['/ordem-servico'])
      });
    } else {
      // CRIAR
      this.service.salvar(os).subscribe({
        next: () => this.router.navigate(['/ordem-servico'])
      });
    }
    console.log("ENDEREÇO: ", os.endereco);
    console.log('OBJETO', this.montarObjeto());
    console.log('OS', os);
  }

  buscarClientePorCodigo() {
    const codigo = this.camposForm.get('codigoCliente')?.value;
    if (!codigo) {
      this.camposForm.patchValue({ nomeCliente: '', clienteId: null });
      return;
    }

    this.clienteService.buscarPorCodigo(codigo).pipe(
      catchError(err => {
        // Se não encontrar cliente ou erro na busca
        this.camposForm.patchValue({ nomeCliente: '', clienteId: null });
        return of(null);
      })
    ).subscribe(cliente => {
      if (cliente) {
        this.camposForm.patchValue({
          nomeCliente: cliente.razaoSocial,
          clienteId: cliente.id
        });
      } else {
        this.camposForm.patchValue({
          nomeCliente: '',
          clienteId: null
        });
      }
    });
  }

  buscarCredenciadoPorCodigo() {
    const codigo = this.camposForm.get('codigoCredenciado')?.value;
    if (!codigo) {
      this.camposForm.patchValue({ 
        nomeCredenciado: '', 
        credenciadoId: null 
      });
      return;
    }

    this.credenciadoService.buscarPorCodigo(codigo).pipe(
      catchError(err => {
        this.camposForm.patchValue({ 
          nomeCredenciado: '', 
          credenciadoId: null 
        });
        return of(null);
      })
    ).subscribe(credenciado => {
      if (credenciado) {
        this.camposForm.patchValue({
          nomeCredenciado: credenciado.rag,
          credenciadoId: credenciado.id
        });
      } else {
        this.camposForm.patchValue({
          nomeCredenciado: '',
          credenciadoId: null
        });
      }
    });
  }


  /** Transforma campos isolados → objeto OrdemServico completo */
  private montarObjeto(): OrdemServico {
    const f = this.camposForm.getRawValue();

    return {
      id: f.id,

      osClt: f.osClt,
      osg: f.osg,
      status: f.status,

      dataHora: f.dataHora, // string ISO

      clienteId: f.clienteId,
      credenciadoId: f.credenciadoId,


      contrato: f.contrato,
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

    if (!cep || cep.length < 8) {
      return;
    }

    this.service.buscarCep(cep).subscribe({
      next: (dados) => {
        this.camposForm.patchValue({
          logradouro: dados.logradouro,
          bairro: dados.bairro,
          cidade: dados.localidade,
          estado: dados.uf,
          complemento: dados.complemento
        });
      },
      error: () => {
        console.error("CEP não encontrado");
      }
    });
  }

}
