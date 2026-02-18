import { Component, OnInit } from '@angular/core';
import { Page } from '../../template/utils/page';
import { Solucao } from '../solucao';
import { SolucaoService } from '../solucao.service';

@Component({
  selector: 'app-consulta-solucao',
  standalone: false,
  templateUrl: './consulta-solucao.component.html',
  styleUrl: './consulta-solucao.component.scss'
})
export class ConsultaSolucaoComponent implements OnInit {

  solucoes: Solucao[] = [];

  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;

  loading = false;
  errorMessage = '';

  constructor(private service: SolucaoService) { }

  ngOnInit(): void {
    this.carregarSolucoes();
  }

  carregarSolucoes() {
    this.loading = true;

    this.service.listar(this.page, this.size).subscribe({
      next: (res: Page<Solucao>) => {
        this.solucoes = res.content ?? [];
        this.totalPages = res.totalPages;
        this.totalElements = res.totalElements;
        this.loading = false;
      },
      error: err => {
        console.error(err);
        this.errorMessage = 'Erro ao carregar soluções';
        this.loading = false;
      }
    });
  }

  paginaAnterior() {
    if (this.page > 0) {
      this.page--;
      this.carregarSolucoes();
    }
  }

  proximaPagina() {
    if ((this.page + 1) < this.totalPages) {
      this.page++;
      this.carregarSolucoes();
    }
  }
}
