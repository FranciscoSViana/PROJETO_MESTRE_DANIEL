import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CadastroCredenciadoComponent } from './cadastro-credenciado/cadastro-credenciado.component';
import { ConsultaCredenciadoComponent } from './consulta-credenciado/consulta-credenciado.component';
import { TecnicosComponent } from './tecnicos/tecnicos.component';

const routes: Routes = [
  {
    path: 'cadastro',
    component: CadastroCredenciadoComponent
  },
  {
    path: '',
    component: ConsultaCredenciadoComponent
  },
  {
    path: 'editar/:id',
    component: CadastroCredenciadoComponent
  },
  {
    path: ':id/tecnicos',
    component: TecnicosComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CredenciadosRoutingModule { }
