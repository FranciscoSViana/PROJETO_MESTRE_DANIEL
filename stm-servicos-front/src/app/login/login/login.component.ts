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
  forcaSenha = 0;
  forcaSenhaTexto = '';
  forcaSenhaClasse = '';

  constructor(
    private auth: AuthService,
    private router: Router
  ) { }

  login() {
    this.camposForm.markAllAsTouched();

    if (this.camposForm.valid) {
      this.auth.login(this.camposForm.value as { usuario: string, senha: string }).subscribe({
        next: () => {
          console.log('✅ Login realizado com sucesso');
          this.router.navigate(['/clientes']);
        },
        // error: err => {
        //   console.error('❌ Erro no login:', err);
        //   alert('Usuário ou senha inválidos');
        // }
        error: err => {
          if (err.status === 400) {
            alert('Usuário ou senha inválidos.');
          } else if (err.status === 0) {
            alert('Servidor indisponível.');
          } else {
            alert('Erro inesperado ao tentar logar.');
          }
        }

      });
    }
  }

  isCampoInvalido(nomeCampo: string): boolean {
    const campo = this.camposForm.get(nomeCampo);
    return !!(campo?.invalid && campo.touched);
  }

  avaliarForcaSenha() {
    const senha = this.camposForm.get('senha')?.value || '';

    let score = 0;

    if (senha.length >= 8) score += 25;
    if (/[A-Z]/.test(senha)) score += 25;
    if (/[0-9]/.test(senha)) score += 25;
    if (/[^A-Za-z0-9]/.test(senha)) score += 25;

    this.forcaSenha = score;

    if (score <= 25) {
      this.forcaSenhaTexto = 'Senha fraca';
      this.forcaSenhaClasse = 'bg-red-500';
    } else if (score <= 50) {
      this.forcaSenhaTexto = 'Senha média';
      this.forcaSenhaClasse = 'bg-yellow-500';
    } else if (score <= 75) {
      this.forcaSenhaTexto = 'Senha boa';
      this.forcaSenhaClasse = 'bg-blue-500';
    } else {
      this.forcaSenhaTexto = 'Senha forte';
      this.forcaSenhaClasse = 'bg-green-500';
    }
  }

  bloquearColar(event: ClipboardEvent) {
    const texto = event.clipboardData?.getData('text') || '';

    const regex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;

    if (!regex.test(texto)) {
      event.preventDefault();
      alert('⚠️ Não é permitido colar senha fraca.');
    }
  }

}
