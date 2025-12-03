import { Component, OnInit } from '@angular/core';
import { LayoutProps } from './layoutprops';
import { ActivatedRoute, Router } from '@angular/router';
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
  usuarioLogado = '';
  isAdmin = false;

  constructor(
    private router: Router, 
    private activatedRoute: ActivatedRoute,
    private auth: AuthService
  ) { }

  ngOnInit(): void {
    this.router.events
      .pipe(
        map(() => this.obterPropriedadesLayout()),
        filter((props): props is LayoutProps => !!props)
      ).subscribe(props => {
        this.props = props;
      });

      this.carregarUsuario();
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
    if (!token) return;

    const payload = JSON.parse(atob(token.split('.')[1]));
    this.usuarioLogado = payload.sub;
    this.isAdmin = payload.roles.some((r: any) => r.authority === 'ROLE_ADMIN');
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
