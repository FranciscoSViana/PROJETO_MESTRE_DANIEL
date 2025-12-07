import { Component } from '@angular/core';
import { UsuarioService } from '../usuario.service';
import { Router } from '@angular/router';
import { Usuario } from '../usuario';

@Component({
  selector: 'app-consulta-usuario',
  standalone: false,
  templateUrl: './consulta-usuario.component.html',
  styleUrl: './consulta-usuario.component.scss'
})
export class ConsultaUsuarioComponent {

  usuarios: Usuario[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  loading = false;
  errorMessage = '';

  constructor(private service: UsuarioService, private router: Router) { }

  ngOnInit(): void {
    this.carregarUsuarios();
  }

  carregarUsuarios() {
    this.loading = true;
    this.errorMessage = '';

    this.service.listarUsuarios().subscribe({
      next: (res) => {
        this.usuarios = res.content ?? [];
        this.totalPages = res.totalPages ?? Math.ceil((res.totalElements ?? this.usuarios.length) / this.size);
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Erro ao carregar usuários';
        this.loading = false;
      }
    });
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
        error: err => alert('Erro ao excluir usuário.')
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
