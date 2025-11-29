import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TemplateRoutingModule } from './template-routing.module';
import { LayoutComponent } from './layout/layout.component';
import { ClientesModule } from '../clientes/clientes.module';
import { CredenciadosModule } from '../credenciados/credenciados.module';


@NgModule({
  declarations: [
    LayoutComponent
  ],
  imports: [
    CommonModule,
    TemplateRoutingModule,
    ClientesModule,
    CredenciadosModule
  ]
})
export class TemplateModule { }
