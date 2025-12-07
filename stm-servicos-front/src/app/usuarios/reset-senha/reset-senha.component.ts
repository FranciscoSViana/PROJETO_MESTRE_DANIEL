import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../auth.service';

@Component({
  selector: 'app-reset-senha',
  standalone: false,
  templateUrl: './reset-senha.component.html',
  styleUrl: './reset-senha.component.scss'
})
export class ResetSenhaComponent implements OnInit {

  token!: string;
  mensagem = '';
  forca = 0;
  classe = '';

  form = new FormGroup({
    senha: new FormControl('', [
      Validators.required,
      Validators.minLength(8)
    ])
  });

  constructor(
    private route: ActivatedRoute,
    private auth: AuthService,
    private router: Router
  ) { }

  ngOnInit() {
    this.token = this.route.snapshot.queryParamMap.get('token')!;

    // ✅ OBSERVA DIGITAÇÃO DA SENHA EM TEMPO REAL
    this.form.get('senha')!.valueChanges.subscribe(value => {
      this.calcularForcaSenha(value || '');
    });
  }

  calcularForcaSenha(senha: string) {
    let forca = 0;

    if (senha.length >= 8) forca += 25;
    if (/[A-Z]/.test(senha)) forca += 25;
    if (/[0-9]/.test(senha)) forca += 25;
    if (/[^A-Za-z0-9]/.test(senha)) forca += 25;

    this.forca = forca;

    if (forca <= 25) {
      this.classe = 'bg-red-500';
    } else if (forca <= 50) {
      this.classe = 'bg-yellow-500';
    } else if (forca <= 75) {
      this.classe = 'bg-blue-500';
    } else {
      this.classe = 'bg-green-500';
    }
  }

  resetar() {
    if (this.form.invalid) return;

    this.auth.resetSenha(this.token, this.form.value.senha!)
      .subscribe(() => {
        this.mensagem = '✅ Senha atualizada com sucesso!';
        setTimeout(() => this.router.navigate(['/']), 2000);
      });
  }
}
