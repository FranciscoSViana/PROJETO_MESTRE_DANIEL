import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-cadastro-usuario',
  standalone: false,
  templateUrl: './cadastro-usuario.component.html',
  styleUrl: './cadastro-usuario.component.scss'
})
export class CadastroUsuarioComponent implements OnInit {

  cadastroForm: FormGroup;
  rolesDisponiveis = ['ADMIN', 'USER'];

  constructor(private service: AuthService, private router: Router) {
    this.cadastroForm = new FormGroup({
      usuario: new FormControl('', Validators.required),
      email: new FormControl('', Validators.required),
      senha: new FormControl('', Validators.required),
      confirmarSenha: new FormControl('', Validators.required),
      roles: new FormControl('', Validators.required)
    });

    // Verifica se senhas conferem
    this.cadastroForm.valueChanges.subscribe(() => {
      const senha = this.cadastroForm.get('senha')?.value;
      const confirmar = this.cadastroForm.get('confirmarSenha')?.value;

      if (senha && confirmar && senha !== confirmar) {
        this.cadastroForm.get('confirmarSenha')?.setErrors({ naoConfere: true });
      } else {
        this.cadastroForm.get('confirmarSenha')?.setErrors(null);
      }
    });
  }

  ngOnInit(): void { }

  salvar() {
    this.cadastroForm.markAllAsTouched();

    if (this.cadastroForm.valid) {
      const formValue = this.cadastroForm.getRawValue();

      // Converte roles de string para array (ex.: "ADMIN,USER" → ["ADMIN","USER"])
      const rolesArray = formValue.roles.split(',').map((r: string) => r.trim());

      const usuario = {
        usuario: formValue.usuario,
        email: formValue.email,
        senha: formValue.senha,
        roles: rolesArray,
        enabled: true
      };

      this.service.cadastrar(usuario).subscribe({
        next: () => {
          alert('Usuário cadastrado com sucesso!');
          this.router.navigate(['/']);
        },
        error: err => {
          console.error('Erro ao cadastrar usuário', err);
          const msg = err.error?.userMessage || 'Erro ao cadastrar usuário'
          alert(msg);
        }
      });
    }
  }

  isCampoInvalido(nomeCampo: string): boolean {
    const campo = this.cadastroForm.get(nomeCampo);
    return campo?.invalid && campo.touched && campo.errors?.['required'];
  }
}
