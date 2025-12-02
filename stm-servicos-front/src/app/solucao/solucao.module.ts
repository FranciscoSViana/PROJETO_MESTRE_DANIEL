import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SolucaoRoutingModule } from './solucao-routing.module';
import { CadastroSolucaoComponent } from './cadastro-solucao/cadastro-solucao.component';


@NgModule({
  declarations: [
    CadastroSolucaoComponent
  ],
  imports: [
    CommonModule,
    SolucaoRoutingModule
  ]
})
export class SolucaoModule { }
