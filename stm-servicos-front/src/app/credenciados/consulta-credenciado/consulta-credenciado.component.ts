import { Component, OnInit } from '@angular/core';
import { CredenciadoService } from '../credenciado.service';
import { Credenciado } from '../credenciado';
import { Router } from '@angular/router';

@Component({
  selector: 'app-consulta-credenciado',
  standalone: false,
  templateUrl: './consulta-credenciado.component.html',
  styleUrl: './consulta-credenciado.component.scss'
})
export class ConsultaCredenciadoComponent implements OnInit {

  credenciados: Credenciado[] = [];
  totalElements = 0;
  page = 0;
  size = 10;
  totalPages = 0;
  loading = false;
  errorMessage = '';

  constructor(private service: CredenciadoService, private router: Router) { }

  ngOnInit(): void {
    this.carregarCredenciados();
  }

  carregarCredenciados() {
    this.loading = true;
    this.errorMessage = '';

    this.service.listar(this.page, this.size).subscribe({
      next: res => {
        this.credenciados = res.content ?? [];
        this.totalElements = res.totalElements ?? this.credenciados.length;

        if (typeof res.totalPages === 'number') {
          this.totalPages = res.totalPages;
        } else {
          this.totalPages = Math.ceil((this.totalElements ?? 0) / this.size);
        }

        this.loading = false;
      },
      error: err => {
        console.error('Erro ao carregar credenciados', err);
        this.errorMessage = 'Erro ao carregar credenciados';
        this.loading = false;
      }
    });
  }

  excluir(id?: string) {
    if (!id) {
      alert('ID inválido.');
      return;
    }

    if (confirm('Deseja realmente excluir este credenciado?')) {
      this.service.excluir(id).subscribe({
        next: () => this.carregarCredenciados(),
        error: err => {
          console.error('Erro ao excluir', err);
          alert('Erro ao excluir credenciado.');
        }
      });
    }
  }

  paginaAnterior() {
    if (this.page > 0) {
      this.page--;
      this.carregarCredenciados();
    }
  }

  proximaPagina() {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.carregarCredenciados();
    }
  }

  formatarCpf(cpf: string | null | undefined): string {
    if (!cpf) return '';
    const num = cpf.toString().padStart(11, '0');
    return num.replace(/^(\d{3})(\d{3})(\d{3})(\d{2})$/, "$1.$2.$3-$4");
  }

  editar(id?: string) {
    console.log('🟢 Clicou no editar, ID:', id);
    this.router.navigate(['/credenciados/editar', id]);
  }

  irParaTecnicos(id?: string) {
    if (!id) return;

    this.router.navigate(['/credenciados', id, 'tecnicos']);
  }
}
