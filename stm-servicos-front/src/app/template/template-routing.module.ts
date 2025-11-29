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
        loadChildren: () => import('../clientes/clientes.module').then(m => m.ClientesModule)
      },
      {
        path: 'credenciados',
        loadChildren: () => import('../credenciados/credenciados.module').then(m => m.CredenciadosModule)
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TemplateRoutingModule { }
