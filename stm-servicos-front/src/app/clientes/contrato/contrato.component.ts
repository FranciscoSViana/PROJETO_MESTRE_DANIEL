import { Component, OnInit } from '@angular/core';
import { Contrato } from '../contrato';
import { ActivatedRoute } from '@angular/router';
import { ClienteService } from '../cliente.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Cliente } from '../cliente';

@Component({
  selector: 'app-contrato',
  standalone: false,
  templateUrl: './contrato.component.html',
  styleUrl: './contrato.component.scss'
})
export class ContratoComponent implements OnInit {

  cliente?: Cliente;
  clienteId!: string;
  contratos: Contrato[] = [];
  loading = false;
  modalAberto = false;
  contratoForm = new FormGroup({
    numeroContrato: new FormControl('', Validators.required),
    nomeContrato: new FormControl('', Validators.required),
    valorChamado: new FormControl<number | null>(null),
    valorKm: new FormControl<number | null>(null),
    responsavelContrato: new FormControl('', Validators.required),
    telefoneContrato: new FormControl('', Validators.required)
  });
  contratoEmEdicao?: Contrato;


  constructor(private route: ActivatedRoute, private service: ClienteService) { }

  ngOnInit(): void {
    this.clienteId = this.route.snapshot.paramMap.get('id')!;
    this.carregarCliente();
  }

  salvarContrato() {
    if (this.contratoForm.invalid) {
      this.contratoForm.markAllAsTouched();
      return;
    }

    const contratoPayload = this.contratoForm.value as Contrato;

    // 🔁 EDITAR
    if (this.contratoEmEdicao?.id) {
      this.service
        .atualizarContrato(
          this.clienteId,
          this.contratoEmEdicao.id,
          contratoPayload
        )
        .subscribe({
          next: () => {
            this.fecharModal();
            this.carregarCliente();
          },
          error: err => {
            console.error('Erro ao atualizar contrato', err);
            alert('Erro ao atualizar contrato');
          }
        });

      return;
    }

    // ➕ NOVO
    this.service.adicionarContratos(this.clienteId, contratoPayload)
      .subscribe({
        next: () => {
          this.fecharModal();
          this.carregarCliente();
        },
        error: err => {
          console.error('Erro ao salvar contrato', err);
          alert('Erro ao salvar contrato');
        }
      });
  }

  excluirContrato(contrato: Contrato) {
    if (!contrato.id) return;

    if (!confirm(`Deseja excluir o contrato ${contrato.numeroContrato}?`)) {
      return;
    }

    this.service
      .excluirContrato(this.clienteId, contrato.id)
      .subscribe({
        next: () => this.carregarCliente(),
        error: err => {
          console.error('Erro ao excluir contrato', err);
          alert('Erro ao excluir contrato');
        }
      });
  }

  formatarCnpj(cnpj?: string): string {
    if (!cnpj) return '';
    return cnpj.replace(
      /^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})$/,
      '$1.$2.$3/$4-$5'
    );
  }

  carregarContratos() {
    this.loading = true;

    this.service.buscarPorId(this.clienteId).subscribe({
      next: cliente => {
        this.contratos = cliente.contratos ?? [];
        this.loading = false;
      },
      error: err => {
        console.log('Erro ao carregar contratos', err);
        this.loading = false;
      }
    });
  }

  carregarCliente() {
    this.loading = true;

    this.service.buscarPorId(this.clienteId).subscribe({
      next: cliente => {
        this.cliente = cliente;                // 👈 ESSENCIAL
        this.contratos = cliente.contratos ?? [];
        this.loading = false;
      },
      error: err => {
        console.error('Erro ao carregar cliente', err);
        this.loading = false;
      }
    });
  }

  abrirModal() {
    console.log('Botão Novo Contrato clicado');
    this.contratoForm.reset();
    this.modalAberto = true;
  }

  fecharModal() {
    this.modalAberto = false;
  }

  abrirModalNovo() {
    this.contratoEmEdicao = undefined;
    this.contratoForm.reset();
    this.modalAberto = true;
  }

  abrirModalEditar(contrato: Contrato) {
    this.contratoEmEdicao = contrato;

    this.contratoForm.patchValue({
      numeroContrato: contrato.numeroContrato,
      nomeContrato: contrato.nomeContrato,
      valorChamado: contrato.valorChamado,
      valorKm: contrato.valorKm,
      responsavelContrato: contrato.responsavelContrato,
      telefoneContrato: contrato.telefoneContrato
    });

    this.modalAberto = true;
  }

}
