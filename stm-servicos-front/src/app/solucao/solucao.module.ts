import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SolucaoRoutingModule } from './solucao-routing.module';
import { ConsultaSolucaoComponent } from './consulta-solucao/consulta-solucao.component';


@NgModule({
  declarations: [
    ConsultaSolucaoComponent
  ],
  imports: [
    CommonModule,
    SolucaoRoutingModule
  ]
})
export class SolucaoModule { }
