import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TemplateRoutingModule } from './template-routing.module';
import { LayoutComponent } from './layout/layout.component';
import { ClientesModule } from '../clientes/clientes.module';
import { CredenciadosModule } from '../credenciados/credenciados.module';
import { OrdemServicoModule } from '../ordem-servico/ordem-servico.module';
import { LoginModule } from '../login/login.module';
import { UsuariosModule } from '../usuarios/usuarios.module';
import { FinancasModule } from '../financas/financas.module';


@NgModule({
  declarations: [
    LayoutComponent
  ],
  imports: [
    CommonModule,
    TemplateRoutingModule,
    ClientesModule,
    CredenciadosModule,
    OrdemServicoModule,
    LoginModule,
    UsuariosModule,
    FinancasModule
  ]
})
export class TemplateModule { }
