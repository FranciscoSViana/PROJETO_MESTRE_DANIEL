import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ContasPagarComponent } from './contas-pagar/contas-pagar.component';
import { ContasReceberComponent } from './contas-receber/contas-receber.component';

const routes: Routes = [
  {
    path: '',
    component: ContasPagarComponent
  },
  {
    path: 'contas-receber',
    component: ContasReceberComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FinancasRoutingModule { }
