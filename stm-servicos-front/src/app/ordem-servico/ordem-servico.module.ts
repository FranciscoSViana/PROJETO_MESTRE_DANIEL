import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { OrdemServicoRoutingModule } from './ordem-servico-routing.module';
import { CadastroOrdemComponent } from './cadastro-ordem/cadastro-ordem.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgxMaskDirective } from 'ngx-mask';
import { ConsultaOrdemComponent } from './consulta-ordem/consulta-ordem.component';
import { HistoricoOrdemComponent } from './historico-ordem/historico-ordem.component';

@NgModule({
  declarations: [
    CadastroOrdemComponent,
    ConsultaOrdemComponent,
    HistoricoOrdemComponent
  ],
  imports: [
    CommonModule,
    OrdemServicoRoutingModule,
    ReactiveFormsModule,
    FormsModule,
    NgxMaskDirective,
  ]
})
export class OrdemServicoModule { }
