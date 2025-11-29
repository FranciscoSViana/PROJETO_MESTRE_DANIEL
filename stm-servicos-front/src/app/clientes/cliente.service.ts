import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Cliente } from './cliente';
import { Observable } from 'rxjs';
import { Page } from '../template/utils/page';

@Injectable({
  providedIn: 'root'
})
export class ClienteService {

  constructor(private http: HttpClient) { }

  salvar(cliente: Cliente): Observable<Cliente> {
    return this.http.post<Cliente>('http://localhost:8080/api/clientes', cliente);
  }

  listar(page: number = 0, size: number = 10): Observable<Page<Cliente>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http.get<Page<Cliente>>('http://localhost:8080/api/clientes', { params });
  }

  atualizar(id: string, cliente: Cliente): Observable<Cliente> {
    return this.http.put<Cliente>(`http://localhost:8080/api/clientes/${id}`, cliente);
  }


  excluir(id: string): Observable<any> {
    return this.http.delete(`http://localhost:8080/api/clientes/${id}`);
  }

  buscarPorId(id: string): Observable<Cliente> {
    return this.http.get<Cliente>(`http://localhost:8080/api/clientes/${id}`);
  }


  consultarCnpj(cnpj: string): Observable<any> {
    return this.http.get<any>(`http://localhost:8080/api/clientes/cnpj/${cnpj}`);
  }
}
