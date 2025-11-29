import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CadastroCredenciadoComponent } from './cadastro-credenciado/cadastro-credenciado.component';

const routes: Routes = [
  {
    path: 'cadastro',
    component: CadastroCredenciadoComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CredenciadosRoutingModule { }
