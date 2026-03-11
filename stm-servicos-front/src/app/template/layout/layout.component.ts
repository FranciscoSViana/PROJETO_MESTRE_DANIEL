import { Component, OnInit } from '@angular/core';
import { LayoutProps } from './layoutprops';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { filter, map } from 'rxjs';
import { AuthService } from '../../auth.service';

@Component({
  selector: 'app-layout',
  standalone: false,
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.scss'
})
export class LayoutComponent implements OnInit {

  props: LayoutProps = { titulo: '', subTitulo: '' };
  usuarioLogado: string | null = null;
  isAdmin = false;
  menuUsuarioAberto = false;
  menuMobileAberto = false;

  anoAtual: number = new Date().getFullYear();

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private auth: AuthService
  ) { }

  ngOnInit(): void {
    // Atualiza props a cada navegação
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.props = this.obterPropriedadesLayout() || { titulo: '', subTitulo: '' };
      });

    // Observa mudanças no usuário logado
    this.auth.usuario$.subscribe(usuario => {
      this.usuarioLogado = usuario;
      this.isAdmin = this.auth.isAdmin();
    });
  }

  obterPropriedadesLayout(): LayoutProps | null {
    let rotaFilha = this.activatedRoute.firstChild;

    while (rotaFilha?.firstChild) {
      rotaFilha = rotaFilha.firstChild;
    }

    const data = rotaFilha?.snapshot.data;

    if (data && 'titulo' in data && 'subTitulo' in data) {
      return data as LayoutProps;
    }

    return null;
  }

  carregarUsuario() {
    const token = localStorage.getItem('token');
    if (!token) {
      this.usuarioLogado = null; // garante que nav não aparece
      return;
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const agora = Math.floor(Date.now() / 1000);

      // verifica expiração
      if (payload.exp && payload.exp < agora) {
        this.usuarioLogado = null;
        localStorage.removeItem('token'); // opcional
        return;
      }

      this.usuarioLogado = payload.sub;
      this.isAdmin = payload.roles.some((r: any) => r.authority === 'ROLE_ADMIN');

    } catch (e) {
      this.usuarioLogado = null; // token inválido
      localStorage.removeItem('token');
    }
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/']);
  }

  toggleMenuUsuario() {
    this.menuUsuarioAberto = !this.menuUsuarioAberto;
  }

  isLoginRoute(): boolean {
    return this.router.url === '/';
  }

}
