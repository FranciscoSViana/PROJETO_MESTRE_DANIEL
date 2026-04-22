import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FinancasRoutingModule } from './financas-routing.module';
import { ContasPagarComponent } from './contas-pagar/contas-pagar.component';
import { FormsModule } from '@angular/forms';
import { DateRangePickerComponent } from './date-range-picker/date-range-picker.component';
import { ContasReceberComponent } from './contas-receber/contas-receber.component';


@NgModule({
  declarations: [
    ContasPagarComponent,
    DateRangePickerComponent,
    ContasReceberComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    FinancasRoutingModule
  ]
})
export class FinancasModule { }
