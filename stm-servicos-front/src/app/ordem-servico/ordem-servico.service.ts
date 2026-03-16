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

  listar(
    page: number = 0,
    size: number = 10,
    filtro: any = {}
  ): Observable<Page<OrdemServico>> {

    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .append('sort', 'status,asc')
      .append('sort', 'osg,desc');

    // 🔎 Aplicando filtros dinâmicos
    if (filtro.osClt)
      params = params.set('osClt', filtro.osClt);

    if (filtro.osg)
      params = params.set('osg', filtro.osg);

    if (filtro.dataAbertura)
      params = params.set('dataAbertura', filtro.dataAbertura);

    if (filtro.status)
      params = params.set('status', filtro.status);

    if (filtro.cliente)
      params = params.set('cliente', filtro.cliente);

    if (filtro.credenciado)
      params = params.set('credenciado', filtro.credenciado);

    if (filtro.cidade)
      params = params.set('cidade', filtro.cidade);

    if (filtro.estado)
      params = params.set('estado', filtro.estado);

    if (filtro.rastreio)
      params = params.set('rastreio', filtro.rastreio);

    return this.http.get<Page<OrdemServico>>(
      this.apiUrl + '/api/ordens-servico',
      { params }
    );
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

  exportarXlsx(filtro: any = {}): Observable<Blob> {
    let params = this.buildFiltroParams(filtro);
    return this.http.get(this.apiUrl + '/api/ordens-servico/exportar/xlsx', {
      params,
      responseType: 'blob'
    });
  }

  exportarCsv(filtro: any = {}): Observable<Blob> {
    let params = this.buildFiltroParams(filtro);
    return this.http.get(this.apiUrl + '/api/ordens-servico/exportar/csv', {
      params,
      responseType: 'blob'
    });
  }

  exportarPdf(filtro: any = {}): Observable<Blob> {
    let params = this.buildFiltroParams(filtro);
    return this.http.get(this.apiUrl + '/api/ordens-servico/exportar/pdf', {
      params,
      responseType: 'blob'
    });
  }

  atualizarStatusRastreio(id: string, statusRastreio: string): Observable<OrdemServico> {
    return this.http.patch<OrdemServico>(
      this.apiUrl + `/api/ordens-servico/${id}/rastreio`,
      { statusRastreio }
    );
  }

  listarStatusRastreio(): Observable<{ value: string; descricao: string; cor: string }[]> {
    return this.http.get<any[]>(this.apiUrl + '/api/rastreio/status');
  }

  // Método privado para reutilizar a montagem dos filtros
  private buildFiltroParams(filtro: any): HttpParams {
    let params = new HttpParams();
    if (filtro.osClt) params = params.set('osClt', filtro.osClt);
    if (filtro.osg) params = params.set('osg', filtro.osg);
    if (filtro.dataAbertura) params = params.set('dataAbertura', filtro.dataAbertura);
    if (filtro.status) params = params.set('status', filtro.status);
    if (filtro.cliente) params = params.set('cliente', filtro.cliente);
    if (filtro.credenciado) params = params.set('credenciado', filtro.credenciado);
    if (filtro.cidade) params = params.set('cidade', filtro.cidade);
    if (filtro.estado) params = params.set('estado', filtro.estado);
    if (filtro.rastreio) params = params.set('rastreio', filtro.rastreio);
    return params;
  }

  relatorioIndividualPdf(id: string): Observable<Blob> {
    return this.http.get(
      this.apiUrl + `/api/ordens-servico/${id}/relatorio/pdf`,
      { responseType: 'blob' }
    );
  }

  relatorioIndividualXlsx(id: string): Observable<Blob> {
    return this.http.get(
      this.apiUrl + `/api/ordens-servico/${id}/relatorio/xlsx`,
      { responseType: 'blob' }
    );
  }
}
