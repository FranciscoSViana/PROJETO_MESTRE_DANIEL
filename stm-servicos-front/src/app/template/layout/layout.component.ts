import { Component, HostListener, OnInit } from '@angular/core';
import { LayoutProps } from './layoutprops';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService, Notificacao } from '../../auth.service';

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
  notificacoesAbertas = false;
  bannerSenhaDismissed = false;

  notificacoes: Notificacao[] = [];
  qtdNotificacoes = 0;

  anoAtual: number = new Date().getFullYear();

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private auth: AuthService
  ) { }

  ngOnInit(): void {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        this.props = this.obterPropriedadesLayout() || { titulo: '', subTitulo: '' };
        this.menuUsuarioAberto = false;
        this.menuMobileAberto = false;
        this.notificacoesAbertas = false;
      });

    this.auth.usuario$.subscribe(usuario => {
      this.usuarioLogado = usuario;
      this.isAdmin = this.auth.isAdmin();
    });

    this.auth.notificacoes$.subscribe(lista => {
      this.notificacoes = lista;
      this.qtdNotificacoes = lista.filter(n => !n.lida).length;
    });
  }

  // ─── NOTIFICAÇÕES ────────────────────────────

  get temNotificacaoTrocarSenha(): boolean {
    if (this.bannerSenhaDismissed) return false;
    return this.notificacoes.some(n => n.tipo === 'TROCAR_SENHA' && !n.lida);
  }

  toggleNotificacoes() {
    this.notificacoesAbertas = !this.notificacoesAbertas;
    this.menuUsuarioAberto = false;
  }

  marcarTodasLidas() {
    this.auth.marcarNotificacoesComoLidas().subscribe();
    this.notificacoesAbertas = false;
  }

  dispensarBannerSenha() {
    this.bannerSenhaDismissed = true;
  }

  // ─── MENUS ───────────────────────────────────

  toggleMenuUsuario() {
    this.menuUsuarioAberto = !this.menuUsuarioAberto;
    this.notificacoesAbertas = false;
  }

  /**
   * Fecha dropdowns ao clicar fora — mas IGNORA cliques dentro de elementos
   * com atributo [data-menu], para não cancelar ações internas (logout, etc.).
   */
  @HostListener('document:click', ['$event'])
  fecharDropdowns(event: MouseEvent) {
    const alvo = event.target as HTMLElement;
    if (!alvo.closest('[data-menu]')) {
      this.menuUsuarioAberto = false;
      this.notificacoesAbertas = false;
    }
  }

  logout() {
    this.auth.logout();
  }

  isLoginRoute(): boolean {
    return this.router.url === '/';
  }

  obterPropriedadesLayout(): LayoutProps | null {
    let rotaFilha = this.activatedRoute.firstChild;
    while (rotaFilha?.firstChild) rotaFilha = rotaFilha.firstChild;
    const data = rotaFilha?.snapshot.data;
    if (data && 'titulo' in data && 'subTitulo' in data) return data as LayoutProps;
    return null;
  }
}