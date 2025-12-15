import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Credenciado } from './credenciado';
import { Observable } from 'rxjs';
import { Page } from '../template/utils/page';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CredenciadoService {

  apiUrl: string = environment.apiUrl;

  constructor(private http: HttpClient) { }

  salvar(credenciado: Credenciado): Observable<Credenciado> {
    return this.http.post<Credenciado>(this.apiUrl + '/api/credenciados', credenciado);
  }

  listar(page: number = 0, size: number = 10): Observable<Page<Credenciado>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http.get<Page<Credenciado>>(this.apiUrl + '/api/credenciados', { params });
  }

  atualizar(id: string, credenciado: Credenciado): Observable<Credenciado> {
    return this.http.put<Credenciado>(this.apiUrl + `/api/credenciados/${id}`, credenciado);
  }


  excluir(id: string): Observable<any> {
    return this.http.delete(this.apiUrl + `/api/credenciados/${id}`);
  }

  buscarPorId(id: string): Observable<Credenciado> {
    return this.http.get<Credenciado>(this.apiUrl + `/api/credenciados/${id}`);
  }

  buscarPorCodigo(codigo: number): Observable<Credenciado> {
    return this.http.get<Credenciado>(this.apiUrl + `/api/credenciados/credenciado/0${codigo}`);
  }

  /** Lista todos os Estados (UFs) */
  listarEstados(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl + `/api/credenciados/estados`);
  }

  /** Lista os municípios da UF informada */
  listarMunicipios(uf: string): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl + `/api/credenciados/municipios/${uf}`);
  }

  buscarCep(cep: string) : Observable<any> {
    return this.http.get(this.apiUrl + `/api/enderecos/cep/${cep}`);
  }

}
