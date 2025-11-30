import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LayoutComponent } from './layout/layout.component';

const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      {
        path: 'clientes',
        loadChildren: () => import('../clientes/clientes.module').then(m => m.ClientesModule),
        pathMatch: 'full',
        data: { titulo: 'Clientes', subTitulo: 'Módulo de Clientes' }
      },
      {
        path: 'credenciados',
        loadChildren: () => import('../credenciados/credenciados.module').then(m => m.CredenciadosModule),
        pathMatch: 'full',
        data: { titulo: 'Credenciados', subTitulo: 'Módulo de Credenciados' }
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TemplateRoutingModule { }
