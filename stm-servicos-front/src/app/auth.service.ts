import { Injectable } from '@angular/core';
import { environment } from '../environments/environment';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, tap, throwError } from 'rxjs';
import { Router } from '@angular/router';

export interface Notificacao {
  id: string;
  tipo: string;
  mensagem: string;
  lida: boolean;
  criadaEm: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private logoutTimer: any;
  private apiUrl: string = environment.apiUrl + '/api/auth';

  private usuarioSubject = new BehaviorSubject<string | null>(this.getUsuarioDoToken());
  usuario$ = this.usuarioSubject.asObservable();

  private notificacoesSubject = new BehaviorSubject<Notificacao[]>([]);
  notificacoes$ = this.notificacoesSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {
    const token = this.getToken();

    if (token) {
      try {
        const payload: any = JSON.parse(atob(token.split('.')[1]));
        const agora = Math.floor(Date.now() / 1000);

        if (payload.exp && payload.exp < agora) {
          setTimeout(() => this.tentarRenovar(), 100);
        } else {
          this.iniciarTimerExpiracao(token);
          // Carrega notificações ao iniciar se já logado
          setTimeout(() => this.carregarNotificacoes(), 500);
        }
      } catch {
        this.logout();
      }
    }
  }

  // ─── AUTENTICAÇÃO ────────────────────────────

  login(data: { usuario: string; senha: string }) {
    return this.http.post<any>(`${this.apiUrl}/login`, data).pipe(
      tap(res => {
        if (!res.accessToken) return;
        localStorage.setItem('token', res.accessToken);
        localStorage.setItem('refreshToken', res.refreshToken);
        this.iniciarTimerExpiracao(res.accessToken);
        this.atualizarUsuario();
        // Carrega notificações logo após o login
        setTimeout(() => this.carregarNotificacoes(), 300);
      })
    );
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    if (this.logoutTimer) clearTimeout(this.logoutTimer);
    this.atualizarUsuario();
    this.notificacoesSubject.next([]);
    this.router.navigate(['']);
  }

  renovarToken() {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) return throwError(() => new Error('Sem refresh token'));

    return this.http.post<any>(`${this.apiUrl}/refresh`, { refreshToken }).pipe(
      tap(res => {
        localStorage.setItem('token', res.accessToken);
        this.iniciarTimerExpiracao(res.accessToken);
        this.atualizarUsuario();
      })
    );
  }

  // ─── CADASTRO ────────────────────────────────

  cadastrar(usuario: {
    nomeCompleto: string;
    dataNascimento: string;
    email: string;
    senha: string;
    roles: string[];
  }) {
    return this.http.post(`${this.apiUrl}/cadastro`, usuario);
  }

  // ─── TOKEN ───────────────────────────────────

  getToken() {
    return localStorage.getItem('token');
  }

  getRefreshToken() {
    return localStorage.getItem('refreshToken');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getUserRoles(): string[] {
    const token = this.getToken();
    if (!token) return [];
    try {
      const payload: any = JSON.parse(atob(token.split('.')[1]));
      if (Array.isArray(payload.roles)) {
        if (typeof payload.roles[0] === 'string') {
          return payload.roles.map((r: string) => r.replace('ROLE_', ''));
        }
        return payload.roles.map((r: { authority: string }) => r.authority.replace('ROLE_', ''));
      }
      return [];
    } catch {
      return [];
    }
  }

  isAdmin(): boolean {
    return this.getUserRoles().includes('ADMIN');
  }

  getUsuarioDoToken(): string | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const agora = Math.floor(Date.now() / 1000);
      if (payload.exp && payload.exp < agora) return null;
      return payload.sub; // sub = username
    } catch {
      return null;
    }
  }

  atualizarUsuario() {
    this.usuarioSubject.next(this.getUsuarioDoToken());
  }

  // ─── SENHA ───────────────────────────────────

  esqueciSenha(email: string) {
    return this.http.post(`${this.apiUrl}/esqueci-senha`, { email });
  }

  resetSenha(token: string, novaSenha: string) {
    return this.http.post(`${this.apiUrl}/reset-senha`, { token, novaSenha });
  }

  // ─── NOTIFICAÇÕES ────────────────────────────

  carregarNotificacoes() {
    this.http.get<Notificacao[]>(`${this.apiUrl}/notificacoes`).subscribe({
      next: lista => this.notificacoesSubject.next(lista),
      error: () => { /* silencioso */ }
    });
  }

  marcarNotificacoesComoLidas() {
    return this.http.post(`${this.apiUrl}/notificacoes/marcar-lidas`, {}).pipe(
      tap(() => this.notificacoesSubject.next([]))
    );
  }

  get quantidadeNotificacoes(): number {
    return this.notificacoesSubject.value.length;
  }

  // ─── TIMER INTERNO ───────────────────────────

  private iniciarTimerExpiracao(token: string) {
    try {
      if (this.logoutTimer) clearTimeout(this.logoutTimer);
      const payload: any = JSON.parse(atob(token.split('.')[1]));
      const exp = payload.exp;
      if (!exp) return;

      const agora = Math.floor(Date.now() / 1000);
      const tempoRestante = (exp - agora) * 1000;

      if (tempoRestante <= 0) {
        this.tentarRenovar();
        return;
      }

      const tempoParaRenovar = Math.max(tempoRestante - 10000, 0);
      this.logoutTimer = setTimeout(() => this.tentarRenovar(), tempoParaRenovar);
    } catch {
      console.error('Erro ao ler expiração do token');
    }
  }

  private tentarRenovar() {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) { this.logout(); return; }

    this.renovarToken().subscribe({
      next: () => console.log('✅ Token renovado automaticamente'),
      error: () => { this.logout(); }
    });
  }
}