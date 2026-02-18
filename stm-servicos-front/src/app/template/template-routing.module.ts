import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LayoutComponent } from './layout/layout.component';
import { adminGuard } from '../admin.guard';

const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      {
        path: '',
        loadChildren: () => import('../login/login.module').then(m => m.LoginModule),
        data: { titulo: 'Login', subTitulo: 'Entre aqui!' }

      },
      {
        path: 'clientes',
        loadChildren: () => import('../clientes/clientes.module').then(m => m.ClientesModule),
        data: { titulo: 'Clientes', subTitulo: 'Módulo de Clientes' }
      },
      {
        path: 'credenciados',
        loadChildren: () => import('../credenciados/credenciados.module').then(m => m.CredenciadosModule),
        data: { titulo: 'Credenciados', subTitulo: 'Módulo de Credenciados' }
      },
      {
        path: 'ordem-servico',
        loadChildren: () => import('../ordem-servico/ordem-servico.module').then(m => m.OrdemServicoModule),
        data: { titulo: 'Ordens de Serviços', subTitulo: 'Módulo de Ordens' }
      },
      {
        path: 'solucao',
        loadChildren: () => import('../solucao/solucao.module').then(m => m.SolucaoModule),
        data: { titulo: 'Soluções', subTitulo: 'Módulo de Soluções' }
      },
      {
        path: 'usuarios',
        loadChildren: () => import('../usuarios/usuarios.module').then(m => m.UsuariosModule),
        data: { titulo: 'Usuários', subtitulo: 'Gestão do usuário' }
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TemplateRoutingModule { }
