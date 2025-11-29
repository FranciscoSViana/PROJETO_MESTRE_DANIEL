import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CredenciadosRoutingModule } from './credenciados-routing.module';
import { CadastroCredenciadoComponent } from './cadastro-credenciado/cadastro-credenciado.component';
import { ReactiveFormsModule } from '@angular/forms';
import { NgxMaskDirective } from 'ngx-mask';


@NgModule({
  declarations: [
    CadastroCredenciadoComponent
  ],
  imports: [
    CommonModule,
    CredenciadosRoutingModule,
    ReactiveFormsModule,
    NgxMaskDirective
  ]
})
export class CredenciadosModule { }
