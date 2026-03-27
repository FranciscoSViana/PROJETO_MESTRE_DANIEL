import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Page } from '../template/utils/page';
import { ContasPagarItem } from './contas-pagar-item';

@Injectable({
  providedIn: 'root'
})
export class FinancasService {

  private apiUrl = environment.apiUrl + '/api/financeiro/contas-pagar';

  constructor(private http: HttpClient) { }

  listar(page: number, size: number, filtro: any): Observable<Page<ContasPagarItem>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'dataHoraAbertura,desc');

    params = this.buildFiltroParams(params, filtro);
    return this.http.get<Page<ContasPagarItem>>(this.apiUrl, { params });
  }

  exportarXlsx(filtro: any): Observable<Blob> {
    const params = this.buildFiltroParams(new HttpParams(), filtro);
    return this.http.get(`${this.apiUrl}/exportar/xlsx`, { params, responseType: 'blob' });
  }

  exportarPdf(filtro: any): Observable<Blob> {
    const params = this.buildFiltroParams(new HttpParams(), filtro);
    return this.http.get(`${this.apiUrl}/exportar/pdf`, { params, responseType: 'blob' });
  }

  private buildFiltroParams(params: HttpParams, filtro: any): HttpParams {
    if (filtro.osg)         params = params.set('osg', filtro.osg);
    if (filtro.osClt)       params = params.set('osClt', filtro.osClt);
    if (filtro.cliente)     params = params.set('cliente', filtro.cliente);
    if (filtro.credenciado) params = params.set('credenciado', filtro.credenciado);
    if (filtro.pago !== '') params = params.set('pago', filtro.pago);
    if (filtro.lote)        params = params.set('lote', filtro.lote);

    if (filtro.dataAberturaInicio)  params = params.set('dataAberturaInicio',  filtro.dataAberturaInicio);
    if (filtro.dataAberturaFim)     params = params.set('dataAberturaFim',     filtro.dataAberturaFim);
    if (filtro.dataPagamentoInicio) params = params.set('dataPagamentoInicio', filtro.dataPagamentoInicio);
    if (filtro.dataPagamentoFim)    params = params.set('dataPagamentoFim',    filtro.dataPagamentoFim);

    return params;
  }
}