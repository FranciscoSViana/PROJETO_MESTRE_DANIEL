import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FaturamentoRoutingModule } from './faturamento-routing.module';
import { ConsultaFaturamentoComponent } from './consulta-faturamento/consulta-faturamento.component';
import { ContasPagarComponent } from './contas-pagar/contas-pagar.component';


@NgModule({
  declarations: [
    ConsultaFaturamentoComponent,
    ContasPagarComponent
  ],
  imports: [
    CommonModule,
    FaturamentoRoutingModule
  ]
})
export class FaturamentoModule { }
