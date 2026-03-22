import { Component } from '@angular/core';
import { AuthService } from '../../auth.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {

  camposForm = new FormGroup({
    usuario: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    senha: new FormControl('', { nonNullable: true, validators: [Validators.required] })
  });

  constructor(
    private auth: AuthService,
    private router: Router
  ) { }

  login() {
    this.camposForm.markAllAsTouched();
    if (this.camposForm.valid) {
      this.auth.login(this.camposForm.value as { usuario: string, senha: string }).subscribe({
        next: () => {
          this.router.navigate(['/ordem-servico']);
        },
        error: err => {
          if (err.status === 400) {
            alert('Usuário ou senha inválidos.');
          } else if (err.status === 0) {
            alert('Servidor indisponível.');
          }
          // Removido o else genérico que mostrava "Erro inesperado"
        }
      });
    }
  }

  isCampoInvalido(nomeCampo: string): boolean {
    const campo = this.camposForm.get(nomeCampo);
    return !!(campo?.invalid && campo.touched);
  }

}
