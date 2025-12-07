import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UsuarioComponent } from './usuario/usuario.component';
import { CadastroUsuarioComponent } from './cadastro-usuario/cadastro-usuario.component';
import { ResetSenhaComponent } from './reset-senha/reset-senha.component';
import { EsqueciSenhaComponent } from './esqueci-senha/esqueci-senha.component';
import { ConsultaUsuarioComponent } from './consulta-usuario/consulta-usuario.component';

const routes: Routes = [
  {
    path: 'perfil',
    component: UsuarioComponent
  },
  {
    path: 'editar/:id',
    component: UsuarioComponent
  },
  {
    path: 'cadastro',
    component: CadastroUsuarioComponent
  },
  {
    path: '',
    component:ConsultaUsuarioComponent
  },

  // 🔐 RECUPERAÇÃO DE SENHA
  { path: 'esqueci-senha', 
    component: EsqueciSenhaComponent
  },
  { path: 'reset-senha', 
    component: ResetSenhaComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UsuariosRoutingModule { }
