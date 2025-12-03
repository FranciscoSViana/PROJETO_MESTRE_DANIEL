import { Injectable } from '@angular/core';
import { environment } from '../environments/environment.development';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl: string = environment.apiUrl + '/api/auth';

  constructor(private http: HttpClient) { }

  login(data: { usuario: string, senha: string }) {
    return this.http.post<any>(`${this.apiUrl}/login`, data).pipe(
      tap(res => {
        console.log('✅ Resposta do login:', res);

        if (!res.accessToken) {
          console.error('🚨 accessToken não veio na resposta!');
          return;
        }

        localStorage.setItem('token', res.accessToken);
      })
    );
  }


  logout() {
    localStorage.clear();
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

    const payload = JSON.parse(atob(token.split('.')[1]));

    return payload.roles?.map((r: any) => r.authority.replace('ROLE_', '') || []);
  }

  isAdmin(): boolean {
    return this.getUserRoles().includes('ADMIN');
  }
}
