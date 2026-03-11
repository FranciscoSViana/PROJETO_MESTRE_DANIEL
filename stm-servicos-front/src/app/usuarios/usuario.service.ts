import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Usuario } from './usuario';
import { Observable } from 'rxjs';
import { Page } from '../template/utils/page';

@Injectable({
  providedIn: 'root'
})
export class UsuarioService {

  private apiUrl = environment.apiUrl + '/api/auth/usuarios';

  constructor(private http: HttpClient) { }

  atualizarUsuario(id: string, dados: Usuario) {
    return this.http.put(`${this.apiUrl}/${id}`, dados);
  }

  listarUsuarios(page: number = 0, size: number = 10): Observable<Page<Usuario>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http.get<Page<Usuario>>(`${this.apiUrl}`, { params });
  }

  excluirUsuario(id: string) {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
