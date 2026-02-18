import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Page } from '../template/utils/page';
import { Observable } from 'rxjs';
import { Solucao } from './solucao';

@Injectable({
  providedIn: 'root'
})
export class SolucaoService {

  private api = `${environment.apiUrl}/api/solucoes`;

  constructor(private http: HttpClient) { }

  listar(page: number, size: number): Observable<Page<Solucao>> {

    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
      // .set('sort', 'dataAtendimento,desc');

    return this.http.get<Page<Solucao>>(this.api, { params });
  }

  buscarPorId(id: number) {
    return this.http.get<Solucao>(`${this.api}/${id}`);
  }
}
