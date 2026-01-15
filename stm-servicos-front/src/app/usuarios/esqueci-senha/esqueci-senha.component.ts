import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-esqueci-senha',
  standalone: false,
  templateUrl: './esqueci-senha.component.html',
  styleUrl: './esqueci-senha.component.scss'
})
export class EsqueciSenhaComponent {

  mensagem = '';

  form = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email])
  });

  constructor(private auth: AuthService, private router: Router) { }

  enviar() {
    if (this.form.invalid) return;

    this.auth.esqueciSenha(this.form.value.email!)
      .subscribe(() => {

        this.mensagem = '✅ Se o e-mail existir, você receberá instruções. Redirecionando para o login...';

        setTimeout(() => {
          this.router.navigate(['/']);
          // ou ['/login'] se você tiver essa rota explícita
        }, 5000);

      });
  }
}
