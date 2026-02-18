import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ConsultaSolucaoComponent } from './consulta-solucao/consulta-solucao.component';

const routes: Routes = [
  {
    path: '',
    component: ConsultaSolucaoComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SolucaoRoutingModule { }
