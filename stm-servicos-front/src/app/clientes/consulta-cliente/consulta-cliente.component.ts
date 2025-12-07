import { Component, OnInit } from '@angular/core';
import { ClienteService } from '../cliente.service';
import { Cliente } from '../cliente';
import { Router } from '@angular/router';

@Component({
  selector: 'app-consulta-cliente',
  standalone: false,
  templateUrl: './consulta-cliente.component.html',
  styleUrl: './consulta-cliente.component.scss'
})
export class ConsultaClienteComponent implements OnInit {

  clientes: Cliente[] = [];
  totalElements: number = 0;
  page: number = 0;
  size: number = 10;
  totalPages: number = 0;
  loading = false;
  errorMessage = '';

  constructor(private service: ClienteService, private router: Router) { }

  ngOnInit(): void {
    this.carregarClientes();
  }


  carregarClientes() {
    this.loading = true;
    this.errorMessage = '';

    this.service.listar(this.page, this.size).subscribe({
      next: res => {
        // espera-se que o backend retorne um Page<T> com campos:
        // content, totalElements, totalPages (se existir)
        this.clientes = res.content ?? [];
        this.totalElements = res.totalElements ?? (this.clientes.length);
        // prefere totalPages vindo do backend; se não existir, calcula
        if (typeof res.totalPages === 'number') {
          this.totalPages = res.totalPages;
        } else {
          this.totalPages = Math.ceil((this.totalElements ?? 0) / this.size);
        }
        this.loading = false;
      },
      error: err => {
        console.error('Erro ao carregar clientes', err);
        this.errorMessage = 'Erro ao carregar clientes';
        this.loading = false;
      }
    });
  }

  excluir(id?: string) {
    if (!id) {
      alert('ID inválido para exclusão.');
      return;
    }

    if (confirm('Tem certeza que deseja excluir este cliente?')) {
      this.service.excluir(id).subscribe({
        next: () => this.carregarClientes(),
        error: err => {
          console.error('Erro ao excluir cliente', err);
          alert('Erro ao excluir cliente.');
        }
      });
    }
  }

  paginaAnterior() {
    if (this.page > 0) {
      this.page--;
      this.carregarClientes();
    }
  }

  proximaPagina() {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.carregarClientes();
    }
  }

  formatarCnpj(cnpj: string | number | null | undefined): string {
    if (!cnpj) return '';

    const num = cnpj.toString().padStart(14, '0');

    return num.replace(
      /^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})$/,
      "$1.$2.$3/$4-$5"
    );
  }

  editar(id?: string) {
    console.log('🟢 Clicou no editar, ID:', id);
    this.router.navigate(['/clientes/editar', id]);
  }
}
