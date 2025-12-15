import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { TecnicosRoutingModule } from './tecnicos-routing.module';
import { CadastroTecnicoComponent } from './cadastro-tecnico/cadastro-tecnico.component';


@NgModule({
  declarations: [
    CadastroTecnicoComponent
  ],
  imports: [
    CommonModule,
    TecnicosRoutingModule
  ]
})
export class TecnicosModule { }
