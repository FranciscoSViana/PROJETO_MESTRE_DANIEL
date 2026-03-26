import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FinancasRoutingModule } from './financas-routing.module';
import { ContasPagarComponent } from './contas-pagar/contas-pagar.component';
import { FormsModule } from '@angular/forms';


@NgModule({
  declarations: [
    ContasPagarComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    FinancasRoutingModule
  ]
})
export class FinancasModule { }
