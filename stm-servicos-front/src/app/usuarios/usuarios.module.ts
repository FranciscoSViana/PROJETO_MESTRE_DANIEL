import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UsuariosRoutingModule } from './usuarios-routing.module';
import { UsuarioComponent } from './usuario/usuario.component';
import { CadastroUsuarioComponent } from './cadastro-usuario/cadastro-usuario.component';
import { ReactiveFormsModule } from '@angular/forms';
import { ResetSenhaComponent } from './reset-senha/reset-senha.component';
import { EsqueciSenhaComponent } from './esqueci-senha/esqueci-senha.component';
import { ConsultaUsuarioComponent } from './consulta-usuario/consulta-usuario.component';


@NgModule({
  declarations: [
    UsuarioComponent,
    CadastroUsuarioComponent,
    ResetSenhaComponent,
    EsqueciSenhaComponent,
    ConsultaUsuarioComponent
  ],
  imports: [
    CommonModule,
    UsuariosRoutingModule,
    ReactiveFormsModule
  ]
})
export class UsuariosModule { }
