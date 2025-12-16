import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ClientesRoutingModule } from './clientes-routing.module';
import { ClienteComponent } from './cliente/cliente.component';
import { ReactiveFormsModule } from '@angular/forms';
import { NgxMaskDirective } from "ngx-mask";
import { ConsultaClienteComponent } from './consulta-cliente/consulta-cliente.component';
import { ContratoComponent } from './contrato/contrato.component';


@NgModule({
  declarations: [
    ClienteComponent,
    ConsultaClienteComponent,
    ContratoComponent
  ],
  imports: [
    CommonModule,
    ClientesRoutingModule,
    ReactiveFormsModule,
    NgxMaskDirective
]
})
export class ClientesModule { }
