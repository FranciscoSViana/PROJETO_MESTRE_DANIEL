import { Component, OnInit } from '@angular/core';
import { HistoricoOrdemServico } from '../historico-ordem-servico';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { OrdemServicoService } from '../ordem-servico.service';

@Component({
  selector: 'app-historico-ordem',
  standalone: false,
  templateUrl: './historico-ordem.component.html',
  styleUrl: './historico-ordem.component.scss'
})
export class HistoricoOrdemComponent implements OnInit {

  historicos: HistoricoOrdemServico[] = [];
  loading = false;
  errorMessage = '';
  ordemServicoId!: string;

  constructor(
    private route: ActivatedRoute,
    private service: OrdemServicoService,
    private location: Location
  ) { }

  ngOnInit(): void {
    this.ordemServicoId = this.route.snapshot.paramMap.get('id')!;
    this.carregarHistorico();
  }

  carregarHistorico() {
    this.loading = true;
    this.service.buscarHistorico(this.ordemServicoId).subscribe({
      next: res => {
        this.historicos = res;
        this.loading = false;
      },
      error: err => {
        console.error('Erro ao carregar histórico', err);
        this.errorMessage = 'Erro ao carregar histórico da OS';
        this.loading = false;
      }
    });
  }

  voltar(): void {
    this.location.back();
  }
}