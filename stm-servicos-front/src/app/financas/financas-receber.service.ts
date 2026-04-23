import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Page } from '../template/utils/page';
import { ContasReceberItem } from './contas-receber-item';
import { ContasReceberTotais } from './contas-receber-totais';

@Injectable({ providedIn: 'root' })
export class FinancasReceberService {

  private apiUrl = environment.apiUrl + '/api/financeiro/contas-receber';

  constructor(private http: HttpClient) { }

  listar(page: number, size: number, filtro: any): Observable<Page<ContasReceberItem>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'dataHoraAbertura,desc');
    params = this.buildParams(params, filtro);
    return this.http.get<Page<ContasReceberItem>>(this.apiUrl, { params });
  }

  totais(filtro: any): Observable<ContasReceberTotais> {
    const params = this.buildParams(new HttpParams(), filtro);
    return this.http.get<ContasReceberTotais>(`${this.apiUrl}/totais`, { params });
  }

  listarLotes(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/lotes`);
  }

  listarLotesPorCliente(cliente: string): Observable<string[]> {
    const params = new HttpParams().set('cliente', cliente);
    return this.http.get<string[]>(`${this.apiUrl}/lotes-por-cliente`, { params });
  }

  listarOsPendentes(cliente: string, lote: string): Observable<ContasReceberItem[]> {
    const params = new HttpParams().set('cliente', cliente).set('lote', lote);
    return this.http.get<ContasReceberItem[]>(`${this.apiUrl}/os-pendentes`, { params });
  }

  registrarPagamentoLote(payload: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/pagamento-lote`, payload);
  }

  listarClientesDisponiveis(): Observable<string[]> {
    // Busca sem filtro de paginação, traz só os clientes distintos com pago=false
    const params = new HttpParams().set('pago', 'false').set('size', '1000');
    return this.http.get<any>(this.apiUrl, { params }).pipe(
      map((res: any) => {
        const set = new Set<string>(
          (res.content ?? []).map((i: any) => i.cliente).filter(Boolean)
        );
        return Array.from(set).sort() as string[];
      })
    );
  }

  uploadComprovanteRecebimento(file: File, lote: string): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(
      `${environment.apiUrl}/api/uploads/recebimento?osg=${encodeURIComponent(lote)}`,
      formData,
      { responseType: 'text' }
    );
  }

  exportarXlsx(filtro: any): Observable<Blob> {
    const params = this.buildParams(new HttpParams(), filtro);
    return this.http.get(`${this.apiUrl}/exportar/xlsx`, { params, responseType: 'blob' });
  }

  exportarPdf(filtro: any): Observable<Blob> {
    const params = this.buildParams(new HttpParams(), filtro);
    return this.http.get(`${this.apiUrl}/exportar/pdf`, { params, responseType: 'blob' });
  }

  private buildParams(params: HttpParams, filtro: any): HttpParams {
    if (filtro.osg) params = params.set('osg', filtro.osg);
    if (filtro.osClt) params = params.set('osClt', filtro.osClt);
    if (filtro.cliente) params = params.set('cliente', filtro.cliente);
    if (filtro.lote) params = params.set('lote', filtro.lote);
    if (filtro.pago !== '') params = params.set('pago', filtro.pago);
    if (filtro.dataAberturaInicio) params = params.set('dataAberturaInicio', filtro.dataAberturaInicio);
    if (filtro.dataAberturaFim) params = params.set('dataAberturaFim', filtro.dataAberturaFim);
    if (filtro.dataPagamentoInicio) params = params.set('dataPagamentoInicio', filtro.dataPagamentoInicio);
    if (filtro.dataPagamentoFim) params = params.set('dataPagamentoFim', filtro.dataPagamentoFim);
    return params;
  }
}