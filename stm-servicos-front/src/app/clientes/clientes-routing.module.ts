import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ClienteComponent } from './cliente/cliente.component';
import { ConsultaClienteComponent } from './consulta-cliente/consulta-cliente.component';

const routes: Routes = [
  {
    path: 'cadastro',
    component: ClienteComponent
  },
  {
    path: '',
    component: ConsultaClienteComponent
  },
  {
    path: 'editar/:id',
    component: ClienteComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ClientesRoutingModule { }
