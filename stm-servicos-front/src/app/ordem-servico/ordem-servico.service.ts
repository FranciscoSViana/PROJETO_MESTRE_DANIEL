import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { OrdemServico } from './ordem-servico';
import { Observable } from 'rxjs';
import { Page } from '../template/utils/page';
import { environment } from '../../environments/environment';
import { Solucao } from '../solucao/solucao';

@Injectable({
  providedIn: 'root'
})
export class OrdemServicoService {

  apiUrl: string = environment.apiUrl;

  constructor(private http: HttpClient) { }

  salvar(os: OrdemServico): Observable<OrdemServico> {
    return this.http.post<OrdemServico>(this.apiUrl + '/api/ordens-servico', os);
  }

  atualizar(id: string, os: OrdemServico): Observable<OrdemServico> {
    return this.http.put<OrdemServico>(this.apiUrl + `/api/ordens-servico/${id}`, os);
  }

  buscarPorId(id: string): Observable<OrdemServico> {
    return this.http.get<OrdemServico>(this.apiUrl + `/api/ordens-servico/${id}`);
  }

  listar(page: number = 0, size: number = 10, sort: string = 'status,asc'): Observable<Page<OrdemServico>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', sort);
    return this.http.get<Page<OrdemServico>>(this.apiUrl + '/api/ordens-servico', { params });
  }

  excluir(id: string): Observable<any> {
    return this.http.delete(this.apiUrl + `/api/ordens-servico/${id}`);
  }

  buscarHistorico(id: string): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl + `/api/ordens-servico/${id}/historico`);
  }

  buscarProximoOsg() {
    return this.http.get(this.apiUrl + '/api/ordens-servico/proximo-osg', { responseType: 'text' });
  }

  buscarCep(cep: string): Observable<any> {
    return this.http.get(this.apiUrl + `/api/enderecos/cep/${cep}`);
  }

  finalizarOS(ordemId: string, payload: Solucao) {
    return this.http.post(this.apiUrl + `/api/ordens-servico/${ordemId}/solucao`, payload);
  }
}
