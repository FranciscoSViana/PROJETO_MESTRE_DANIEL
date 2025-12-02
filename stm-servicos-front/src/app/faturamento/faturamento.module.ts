import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FaturamentoRoutingModule } from './faturamento-routing.module';
import { ConsultaFaturamentoComponent } from './consulta-faturamento/consulta-faturamento.component';


@NgModule({
  declarations: [
    ConsultaFaturamentoComponent
  ],
  imports: [
    CommonModule,
    FaturamentoRoutingModule
  ]
})
export class FaturamentoModule { }
