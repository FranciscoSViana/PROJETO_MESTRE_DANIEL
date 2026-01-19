import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Credenciado } from './credenciado';
import { Observable } from 'rxjs';
import { Page } from '../template/utils/page';
import { environment } from '../../environments/environment';
import { Tecnico } from './tecnico';

@Injectable({
  providedIn: 'root'
})
export class CredenciadoService {

  apiUrl: string = environment.apiUrl;

  constructor(private http: HttpClient) { }

  salvar(credenciado: Credenciado): Observable<Credenciado> {
    return this.http.post<Credenciado>(this.apiUrl + '/api/credenciados', credenciado);
  }

  listar(
    page: number = 0,
    size: number = 10,
    sort: string = 'codigo,asc'
  ): Observable<Page<Credenciado>> {

    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);

    return this.http.get<Page<Credenciado>>(
      this.apiUrl + '/api/credenciados',
      { params }
    );
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
    return this.http.get<Credenciado>(this.apiUrl + `/api/credenciados/credenciado/${codigo}`);
  }

  buscarProximosPorCep(cep: string, raioKm: number = 100): Observable<Credenciado[]> {

    const params = new HttpParams()
      .set('cep', cep)
      .set('raioKm', raioKm.toString());

    return this.http.get<Credenciado[]>(`${this.apiUrl}/api/credenciados/proximos`, { params }
    );
  }


  // ===================== TÉCNICOS =====================

  /** POST /api/credenciados/{credenciadoId}/tecnicos */
  adicionarTecnico(
    credenciadoId: string,
    tecnico: Tecnico
  ): Observable<Tecnico> {
    return this.http.post<Tecnico>(
      `${this.apiUrl}/api/credenciados/${credenciadoId}/tecnicos`,
      tecnico
    );
  }

  /** GET /api/credenciados/{credenciadoId}/tecnicos */
  listarTecnicos(
    credenciadoId: string,
    page: number = 0,
    size: number = 10
  ): Observable<Page<Tecnico>> {

    const params = new HttpParams()
      .set('page', page)
      .set('size', size);

    return this.http.get<Page<Tecnico>>(
      `${this.apiUrl}/api/credenciados/${credenciadoId}/tecnicos`,
      { params }
    );
  }

  /** GET /api/credenciados/tecnicos/{tecnicoId} */
  buscarTecnico(tecnicoId: string): Observable<Tecnico> {
    return this.http.get<Tecnico>(
      `${this.apiUrl}/api/credenciados/tecnicos/${tecnicoId}`
    );
  }

  /** PUT /api/credenciados/tecnicos/{tecnicoId} */
  atualizarTecnico(
    tecnicoId: string,
    tecnico: Tecnico
  ): Observable<Tecnico> {

    console.group('🟠 [HTTP] PUT Técnico');
    console.log('URL:', `${this.apiUrl}/api/credenciados/tecnicos/${tecnicoId}`);
    console.log('Body:', tecnico);
    console.log('Endereço no body:', tecnico?.endereco);
    console.groupEnd();

    return this.http.put<Tecnico>(
      `${this.apiUrl}/api/credenciados/tecnicos/${tecnicoId}`,
      tecnico
    );
  }

  /** DELETE /api/credenciados/tecnicos/{tecnicoId} */
  excluirTecnico(tecnicoId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/api/credenciados/tecnicos/${tecnicoId}`
    );
  }

  /** Lista todos os Estados (UFs) */
  listarEstados(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl + `/api/credenciados/estados`);
  }

  /** Lista os municípios da UF informada */
  listarMunicipios(uf: string): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl + `/api/credenciados/municipios/${uf}`);
  }

  buscarCep(cep: string): Observable<any> {
    return this.http.get(this.apiUrl + `/api/enderecos/cep/${cep}`);
  }

}
