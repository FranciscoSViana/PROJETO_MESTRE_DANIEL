import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { OrdemServico } from './ordem-servico';
import { Observable } from 'rxjs';
import { Page } from '../template/utils/page';

@Injectable({
  providedIn: 'root'
})
export class OrdemServicoService {

  constructor(private http: HttpClient) { }

  salvar(os: OrdemServico): Observable<OrdemServico> {
    return this.http.post<OrdemServico>('http://localhost:8080/api/ordens-servico', os);
  }

  atualizar(id: string, os: OrdemServico): Observable<OrdemServico> {
    return this.http.put<OrdemServico>(`http://localhost:8080/api/ordens-servico/${id}`, os);
  }

  buscarPorId(id: string): Observable<OrdemServico> {
    return this.http.get<OrdemServico>(`http://localhost:8080/api/ordens-servico/${id}`);
  }

  listar(page: number = 0, size: number = 10): Observable<Page<OrdemServico>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<OrdemServico>>('http://localhost:8080/api/ordens-servico', { params });
  }

  excluir(id: string): Observable<any> {
    return this.http.delete(`http://localhost:8080/api/ordens-servico/${id}`);
  }

  buscarProximoOsg() {
    return this.http.get('http://localhost:8080/api/ordens-servico/proximo-osg', {responseType: 'text'});
  }

  buscarCep(cep: string) : Observable<any> {
    return this.http.get(`http://localhost:8080/api/enderecos/cep/${cep}`);
  }
}
