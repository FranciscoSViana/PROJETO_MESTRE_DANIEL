import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ContasPagarComponent } from './contas-pagar/contas-pagar.component';

const routes: Routes = [
  {
    path: '',
    component: ContasPagarComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class FinancasRoutingModule { }
