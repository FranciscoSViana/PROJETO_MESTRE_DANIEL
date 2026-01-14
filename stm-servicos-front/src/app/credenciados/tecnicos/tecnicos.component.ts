import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Tecnico } from '../tecnico';
import { CredenciadoService } from '../credenciado.service';
import { Credenciado } from '../credenciado';
import { FormControl, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-tecnicos',
  standalone: false,
  templateUrl: './tecnicos.component.html',
  styleUrl: './tecnicos.component.scss'
})
export class TecnicosComponent implements OnInit {

  credenciadoId!: string;
  credenciado?: Credenciado;
  tecnicos: Tecnico[] = [];

  tipoDocumentoLabel: string = '';

  loading = false;
  modalAberto = false;

  page = 0;
  size = 10;
  totalPages = 0;

  tecnicoForm = new FormGroup({
    nome: new FormControl('', Validators.required),
    cpf: new FormControl('', Validators.required),
    telefone: new FormControl(''),
    email: new FormControl(''),

    endereco: new FormGroup({
      logradouro: new FormControl<string | null>(null),
      numero: new FormControl<string | null>(null),
      bairro: new FormControl<string | null>(null),
      cidade: new FormControl<string | null>(null),
      estado: new FormControl<string | null>(null),
      complemento: new FormControl<string | null>(null),
      cep: new FormControl<string | null>(null),
    })
  });

  tecnicoEditandoId: string | null = null;
  modoEdicao = false;

  constructor(private route: ActivatedRoute, private service: CredenciadoService) { }

  ngOnInit(): void {
    this.credenciadoId = this.route.snapshot.paramMap.get('id')!;
    this.carregarCredenciado();
    this.carregarTecnicos();
  }

  carregarCredenciado() {
    this.service.buscarPorId(this.credenciadoId).subscribe({
      next: credenciado => {
        this.credenciado = credenciado
        this.tipoDocumentoLabel = this.getTipoDocumentoLabel(credenciado.tipoPessoa)
      },
      error: err => console.error('Erro ao carregar credenciado', err)
    });
  }

  private getTipoDocumentoLabel(tipoPessoa: number | string | undefined): string {
    if (tipoPessoa === 1) return 'CPF';
    if (tipoPessoa === 2) return 'CNPJ';
    return '';
  }

  carregarTecnicos() {
    this.loading = true;

    this.service.listarTecnicos(this.credenciadoId).subscribe({
      next: res => {
        this.tecnicos = res.content ?? [];
        this.loading = false;
      },
      error: err => {
        console.error('Erro ao carregar técnicos', err);
        this.loading = false;
      }
    });
  }

  formatarTelefone(telefone?: string): string {
    if (!telefone) return '';

    // remove tudo que não for número
    const t = telefone.replace(/\D/g, '');

    // celular com DDD (11 dígitos)
    if (t.length === 11) {
      return t.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
    }

    // fixo com DDD (10 dígitos)
    if (t.length === 10) {
      return t.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
    }

    return telefone;
  }

  formatarCpf(cpf: string | null | undefined): string {
    if (!cpf) return '';
    const num = cpf.toString().padStart(11, '0');
    return num.replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, "$1.$2.$3-$4");
  }

  abrirModal() {
    this.modoEdicao = false;
    this.tecnicoEditandoId = null;
    this.tecnicoForm.reset();
    this.modalAberto = true;
  }

  fecharModal() {
    this.modalAberto = false;
  }

  editarTecnico(tecnico: Tecnico) {
    this.modoEdicao = true;
    this.tecnicoEditandoId = tecnico.id!;
    this.modalAberto = true;

    this.tecnicoForm.patchValue({
      nome: tecnico.nome,
      cpf: tecnico.cpf,
      telefone: tecnico.telefone,
      email: tecnico.email,
      endereco: {
        logradouro: tecnico.endereco?.logradouro,
        numero: tecnico.endereco?.numero,
        bairro: tecnico.endereco?.bairro,
        cidade: tecnico.endereco?.cidade,
        estado: tecnico.endereco?.estado,
        complemento: tecnico.endereco?.complemento,
        cep: tecnico.endereco?.cep
      }
    });
  }

  salvarTecnico() {
    if (this.tecnicoForm.invalid) {
      this.tecnicoForm.markAllAsTouched();
      return;
    }

    const payload = this.tecnicoForm.value as any;

    if (this.modoEdicao && this.tecnicoEditandoId) {
      // 🔁 ATUALIZAR
      this.service.atualizarTecnico(this.tecnicoEditandoId, payload)
        .subscribe({
          next: () => {
            this.fecharModal();
            this.carregarTecnicos();
          },
          error: err => {
            console.error('Erro ao atualizar técnico', err);
            alert('Erro ao atualizar técnico');
          }
        });
    } else {
      // ➕ CRIAR
      this.service.adicionarTecnico(this.credenciadoId, payload)
        .subscribe({
          next: () => {
            this.fecharModal();
            this.carregarTecnicos();
          },
          error: err => {
            console.error('Erro ao salvar técnico', err);
            alert('Erro ao salvar técnico');
          }
        });
    }
  }

  excluirTecnico(tecnico: Tecnico) {
    if (!tecnico.id) return;

    const confirmar = confirm(`Deseja realmente excluir o técnico ${tecnico.nome}?`);
    if (!confirmar) return;

    this.service.excluirTecnico(tecnico.id).subscribe({
      next: () => {
        this.carregarTecnicos();
      },
      error: err => {
        console.error('Erro ao excluir técnico', err);
        alert('Erro ao excluir técnico');
      }
    });
  }

  buscarCep() {
    const cepControl = this.tecnicoForm.get('endereco.cep');
    const cep = cepControl?.value;

    if (!cep || cep.length < 8) {
      return;
    }

    this.service.buscarCep(cep).subscribe({
      next: dados => {
        this.tecnicoForm.get('endereco')?.patchValue({
          logradouro: dados.logradouro ?? '',
          numero: '', // mantém o que o usuário vai digitar
          bairro: dados.bairro ?? '',
          cidade: dados.localidade ?? '',
          estado: dados.uf ?? '',
          complemento: dados.complemento ?? '',
          cep: cep
        });
      },
      error: () => {
        console.warn('CEP não encontrado');
      }
    });
  }
}
