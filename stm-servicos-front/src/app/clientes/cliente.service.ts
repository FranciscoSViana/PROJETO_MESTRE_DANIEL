import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Cliente } from './cliente';
import { Observable } from 'rxjs';
import { Page } from '../template/utils/page';
import { environment } from '../../environments/environment.development';

@Injectable({
  providedIn: 'root'
})
export class ClienteService {

  apiUrl: string = environment.apiUrl;

  constructor(private http: HttpClient) { }

  salvar(cliente: Cliente): Observable<Cliente> {
    return this.http.post<Cliente>(this.apiUrl + '/api/clientes', cliente);
  }

  listar(page: number = 0, size: number = 10): Observable<Page<Cliente>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http.get<Page<Cliente>>(this.apiUrl + '/api/clientes', { params });
  }

  atualizar(id: string, cliente: Cliente): Observable<Cliente> {
    return this.http.put<Cliente>(this.apiUrl + `/api/clientes/${id}`, cliente);
  }

  excluir(id: string): Observable<any> {
    return this.http.delete(this.apiUrl + `/api/clientes/${id}`);
  }

  buscarPorId(id: string): Observable<Cliente> {
    return this.http.get<Cliente>(this.apiUrl + `/api/clientes/${id}`);
  }

  buscarPorCodigo(codigo: number): Observable<Cliente> {
    return this.http.get<Cliente>(this.apiUrl + `/api/clientes/codigo/${codigo}`);
  }

  consultarCnpj(cnpj: string): Observable<any> {
    return this.http.get<any>(this.apiUrl + `/api/clientes/cnpj/${cnpj}`);
  }
}
