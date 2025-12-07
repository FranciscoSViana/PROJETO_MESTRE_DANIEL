import { Component, OnInit } from '@angular/core';
import { OrdemServico } from '../ordem-servico';
import { OrdemServicoService } from '../ordem-servico.service';
import { ClienteService } from '../../clientes/cliente.service';
import { CredenciadoService } from '../../credenciados/credenciado.service';
import { forkJoin, map } from 'rxjs';
import { Router } from '@angular/router';

@Component({
  selector: 'app-consulta-ordem',
  standalone: false,
  templateUrl: './consulta-ordem.component.html',
  styleUrl: './consulta-ordem.component.scss'
})
export class ConsultaOrdemComponent implements OnInit {

  ordensServico: OrdemServico[] = [];
  totalElements: number = 0;
  page: number = 0;
  size: number = 10;
  totalPages: number = 0;
  loading = false;
  errorMessage = '';

  clientes: any[] = [];
  credenciados: any[] = [];

  constructor(
    private service: OrdemServicoService,
    private clienteService: ClienteService,
    private credenciadoService: CredenciadoService,
    private router: Router
  ) { }

  ngOnInit(): void {
    forkJoin({
      clientes: this.clienteService.listar().pipe(
        map(pageClientes => pageClientes.content ?? [])
      ),
      credenciados: this.credenciadoService.listar().pipe(
        map(pageCredenciados => pageCredenciados.content ?? [])
      )
    }).subscribe(({ clientes, credenciados }) => {
      this.clientes = clientes;
      this.credenciados = credenciados;

      // Agora que temos os clientes e credenciados, podemos carregar as OS
      this.carregarOrdens();
    });
  }

  carregarOrdens() {
    this.loading = true;
    this.errorMessage = '';

    this.service.listar(this.page, this.size).subscribe({
      next: res => {

        console.log('Ordens recebidas do backend:', res.content);

        res.content.forEach(os => {
          console.log('OS', os.id, 'clienteId', os.clienteId, 'credenciadoId', os.credenciadoId);
          console.log('Cliente encontrado', this.clientes.find(c => c.id === os.clienteId));
          console.log('Credenciado encontrado', this.credenciados.find(c => c.id === os.credenciadoId));
        });

        // Enriquecer OS com nomes
        this.ordensServico = (res.content ?? []).map(os => ({
          ...os,
          clienteNome: os.cliente?.razaoSocial || os.cliente?.nome || '-',
          credenciadoNome: os.credenciado?.tecnico || '-'
        }));

        this.totalElements = res.totalElements ?? this.ordensServico.length;
        this.totalPages = typeof res.totalPages === 'number' ? res.totalPages :
          Math.ceil(this.totalElements / this.size);
        this.loading = false;
      },
      error: err => {
        console.error('Erro ao carregar OS', err);
        this.errorMessage = 'Erro ao carregar Ordens de Serviço';
        this.loading = false;
      }
    });
  }


  excluir(id?: number) {
    if (!id) return alert('ID inválido para exclusão.');

    if (confirm('Tem certeza que deseja excluir esta OS?')) {
      this.service.excluir(id.toString()).subscribe({
        next: () => this.carregarOrdens(),
        error: err => {
          console.error('Erro ao excluir OS', err);
          alert('Erro ao excluir Ordem de Serviço.');
        }
      });
    }
  }

  editar(id?: string) {
    console.log('🟢 Clicou no editar, ID:', id);
    this.router.navigate(['/ordem-servico/editar', id]);
  }

  paginaAnterior() {
    if (this.page > 0) {
      this.page--;
      this.carregarOrdens();
    }
  }

  proximaPagina() {
    if ((this.page + 1) < this.totalPages) {
      this.page++;
      this.carregarOrdens();
    }
  }
}
