import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { OrdemServicoRoutingModule } from './ordem-servico-routing.module';
import { CadastroOrdemComponent } from './cadastro-ordem/cadastro-ordem.component';
import { ReactiveFormsModule } from '@angular/forms';
import { NgxMaskDirective } from 'ngx-mask';
import { ConsultaOrdemComponent } from './consulta-ordem/consulta-ordem.component';


@NgModule({
  declarations: [
    CadastroOrdemComponent,
    ConsultaOrdemComponent
  ],
  imports: [
    CommonModule,
    OrdemServicoRoutingModule,
    ReactiveFormsModule,
    NgxMaskDirective
  ]
})
export class OrdemServicoModule { }
