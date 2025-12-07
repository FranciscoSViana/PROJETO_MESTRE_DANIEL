import { Injectable } from '@angular/core';
import { environment } from '../environments/environment.development';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl: string = environment.apiUrl + '/api/auth';

  // BehaviorSubject para emitir usuário logado
  private usuarioSubject = new BehaviorSubject<string | null>(this.getUsuarioDoToken());
  usuario$ = this.usuarioSubject.asObservable();

  constructor(private http: HttpClient) { }

  login(data: { usuario: string, senha: string }) {
    return this.http.post<any>(`${this.apiUrl}/login`, data).pipe(
      tap(res => {
        if (!res.accessToken) return;

        localStorage.setItem('token', res.accessToken);
        this.atualizarUsuario();
      })
    );
  }

  logout() {
    localStorage.removeItem('token');
    this.atualizarUsuario();
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
}
