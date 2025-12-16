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

  abrirModal() {
    this.tecnicoForm.reset();
    this.modalAberto = true;
  }

  fecharModal() {
    this.modalAberto = false;
  }

  salvarTecnico() {
    if (this.tecnicoForm.invalid) {
      this.tecnicoForm.markAllAsTouched();
      return;
    }

    this.service.adicionarTecnico(this.credenciadoId, this.tecnicoForm.value as any)
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
