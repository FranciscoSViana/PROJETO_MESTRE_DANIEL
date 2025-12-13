import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
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

  listarUsuarios() : Observable<Page<Usuario>> {
    return this.http.get<Page<Usuario>>(`${this.apiUrl}`);
  }

  excluirUsuario(id: string) {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
