import { Injectable } from '@angular/core';
import { environment } from '../environments/environment';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, tap } from 'rxjs';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private logoutTimer: any;

  private apiUrl: string = environment.apiUrl + '/api/auth';

  // BehaviorSubject para emitir usuário logado
  private usuarioSubject = new BehaviorSubject<string | null>(this.getUsuarioDoToken());
  usuario$ = this.usuarioSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {

    const token = this.getToken();

    if (token) {
      this.iniciarTimerExpiracao(token);
    }

  }

  login(data: { usuario: string, senha: string }) {
    return this.http.post<any>(`${this.apiUrl}/login`, data).pipe(
      tap(res => {

        if (!res.accessToken) return;

        localStorage.setItem('token', res.accessToken);

        this.iniciarTimerExpiracao(res.accessToken);

        this.atualizarUsuario();
      })
    );
  }

  logout() {

    localStorage.removeItem('token');

    if (this.logoutTimer) {
      clearTimeout(this.logoutTimer);
    }

    this.atualizarUsuario();

    this.router.navigate(['']);

  }

  cadastrar(usuario: { nome: string, email: string, senha: string, roles: string[] }) {
    return this.http.post(`${this.apiUrl}/cadastro`, usuario);
  }

  getToken() {
    return localStorage.getItem('token');
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
        // Se for array de strings
        if (typeof payload.roles[0] === 'string') {
          return payload.roles.map((r: string) => r.replace('ROLE_', ''));
        }

        // Se for array de objetos { authority: string }
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
      return payload.sub;
    } catch {
      return null;
    }
  }

  atualizarUsuario() {
    this.usuarioSubject.next(this.getUsuarioDoToken());
  }

  esqueciSenha(email: string) {
    return this.http.post(`${this.apiUrl}/esqueci-senha`, { email });
  }

  resetSenha(token: string, novaSenha: string) {
    return this.http.post(`${this.apiUrl}/reset-senha`, { token, novaSenha });
  }

  private iniciarTimerExpiracao(token: string) {

    try {

      const payload: any = JSON.parse(atob(token.split('.')[1]));
      const exp = payload.exp;

      if (!exp) return;

      const agora = Math.floor(Date.now() / 1000);
      const tempoRestante = (exp - agora) * 1000;

      if (tempoRestante <= 0) {
        this.logout();
        return;
      }

      console.log(`⏳ Token expira em ${tempoRestante / 1000}s`);

      this.logoutTimer = setTimeout(() => {

        console.warn('⚠️ Sessão expirada');

        this.logout();

      }, tempoRestante);

    } catch (e) {
      console.error('Erro ao ler expiração do token');
    }

  }
}
