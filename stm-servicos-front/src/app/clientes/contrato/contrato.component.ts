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
    numeroContrato: new FormControl('', Validators.required)
  });

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

    const contrato: Contrato = {
      numeroContrato: this.contratoForm.value.numeroContrato!
    };

    this.service.adicionarContratos(this.clienteId, contrato).subscribe({
      next: () => {
        this.fecharModal();
        this.carregarContratos();
      },
      error: err => {
        console.log('Erro ao salvar contrato', err);
        alert('Erro ao salvar contrato');
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
}
