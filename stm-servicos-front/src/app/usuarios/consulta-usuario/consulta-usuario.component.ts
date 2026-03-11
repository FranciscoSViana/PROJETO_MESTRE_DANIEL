import { Component, OnInit } from '@angular/core';
import { UsuarioService } from '../usuario.service';
import { Router } from '@angular/router';
import { Usuario } from '../usuario';

@Component({
  selector: 'app-consulta-usuario',
  standalone: false,
  templateUrl: './consulta-usuario.component.html',
  styleUrl: './consulta-usuario.component.scss'
})
export class ConsultaUsuarioComponent implements OnInit {

  usuarios: Usuario[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;
  loading = false;
  errorMessage = '';

  pageSizes = [5, 10, 20, 50];

  constructor(private service: UsuarioService, private router: Router) { }

  ngOnInit(): void {
    this.carregarUsuarios();
  }

  carregarUsuarios() {
    this.loading = true;
    this.errorMessage = '';
    this.service.listarUsuarios(this.page, this.size).subscribe({
      next: (res) => {
        this.usuarios = res.content ?? [];
        this.totalElements = res.totalElements ?? this.usuarios.length;

        const page = (res as any).page;
        this.totalPages = page?.totalPages
          ?? res.totalPages
          ?? Math.ceil(this.totalElements / this.size);

        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Erro ao carregar usuários';
        this.loading = false;
      }
    });
  }

  onSizeChange(event: Event) {
    this.size = Number((event.target as HTMLSelectElement).value);
    this.page = 0;
    this.carregarUsuarios();
  }

  editar(id?: string) {
    if (id == null) {
      alert('ID inválido para edição.');
      return;
    }
    this.router.navigate(['/usuarios/cadastro'], { queryParams: { id } });
  }

  excluir(id?: string) {
    if (id == null) {
      alert('ID inválido para exclusão.');
      return;
    }
    if (confirm('Deseja realmente excluir este usuário?')) {
      this.service.excluirUsuario(id).subscribe({
        next: () => this.carregarUsuarios(),
        error: () => alert('Erro ao excluir usuário.')
      });
    }
  }

  paginaAnterior() {
    if (this.page > 0) {
      this.page--;
      this.carregarUsuarios();
    }
  }

  proximaPagina() {
    if (this.page + 1 < this.totalPages) {
      this.page++;
      this.carregarUsuarios();
    }
  }
}