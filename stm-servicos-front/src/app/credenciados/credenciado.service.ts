import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Credenciado } from './credenciado';
import { Observable } from 'rxjs';
import { Page } from '../template/utils/page';

@Injectable({
  providedIn: 'root'
})
export class CredenciadoService {

  constructor(private http: HttpClient) { }

  salvar(credenciado: Credenciado): Observable<Credenciado> {
    return this.http.post<Credenciado>('http://localhost:8080/api/credenciados', credenciado);
  }

  listar(page: number = 0, size: number = 10): Observable<Page<Credenciado>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http.get<Page<Credenciado>>('http://localhost:8080/api/credenciados', { params });
  }

  atualizar(id: string, credenciado: Credenciado): Observable<Credenciado> {
    return this.http.put<Credenciado>(`http://localhost:8080/api/credenciados/${id}`, credenciado);
  }


  excluir(id: string): Observable<any> {
    return this.http.delete(`http://localhost:8080/api/credenciados/${id}`);
  }

  buscarPorId(id: string): Observable<Credenciado> {
    return this.http.get<Credenciado>(`http://localhost:8080/api/credenciados/${id}`);
  }

  /** Lista todos os Estados (UFs) */
  listarEstados(): Observable<any[]> {
    return this.http.get<any[]>(`http://localhost:8080/api/credenciados/estados`);
  }

  /** Lista os municípios da UF informada */
  listarMunicipios(uf: string): Observable<any[]> {
    return this.http.get<any[]>(`http://localhost:8080/api/credenciados/municipios/${uf}`);
  }

}
