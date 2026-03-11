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

  pageSizes = [5, 10, 20, 50];

  constructor(private service: ClienteService, private router: Router) { }

  ngOnInit(): void {
    this.carregarClientes();
  }

  carregarClientes() {
    this.loading = true;
    this.errorMessage = '';
    this.service.listar(this.page, this.size).subscribe({
      next: res => {
        this.clientes = res.content ?? [];
        this.totalElements = res.totalElements ?? this.clientes.length;

        const page = (res as any).page;
        this.totalPages = page?.totalPages
          ?? res.totalPages
          ?? Math.ceil(this.totalElements / this.size);

        this.loading = false;
      },
      error: err => {
        console.error('Erro ao carregar clientes', err);
        this.errorMessage = 'Erro ao carregar clientes';
        this.loading = false;
      }
    });
  }

  onSizeChange(event: Event) {
    const select = event.target as HTMLSelectElement;
    this.size = Number(select.value);
    this.page = 0;
    this.carregarClientes();
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
      '$1.$2.$3/$4-$5'
    );
  }

  editar(id?: string) {
    this.router.navigate(['/clientes/editar', id]);
  }

  abrirContratos(id?: string) {
    if (!id) return;
    this.router.navigate(['/clientes', id, 'contratos']);
  }
}