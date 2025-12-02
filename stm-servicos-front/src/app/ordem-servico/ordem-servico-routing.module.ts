import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CadastroOrdemComponent } from './cadastro-ordem/cadastro-ordem.component';
import { ConsultaOrdemComponent } from './consulta-ordem/consulta-ordem.component';

const routes: Routes = [
  {
    path: '',
    component: ConsultaOrdemComponent
  },
  {
    path: 'cadastro',
    component: CadastroOrdemComponent
  },
  {
    path: 'editar/:id',
    component: CadastroOrdemComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class OrdemServicoRoutingModule { }
