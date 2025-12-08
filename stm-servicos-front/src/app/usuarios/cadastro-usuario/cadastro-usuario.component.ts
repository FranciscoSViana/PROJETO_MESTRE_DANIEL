import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { UsuarioService } from '../usuario.service';

@Component({
  selector: 'app-cadastro-usuario',
  standalone: false,
  templateUrl: './cadastro-usuario.component.html',
  styleUrl: './cadastro-usuario.component.scss'
})
export class CadastroUsuarioComponent implements OnInit {

  cadastroForm: FormGroup;
  rolesDisponiveis = ['ADMIN', 'USER'];
  tituloFormulario = 'Cadastro de Usuário'; // 🎯 título dinâmico
  usuarioId?: string; // para edição
  forcaSenha: number | null = null;
  forcaSenhaTexto = '';
  forcaSenhaClasse = '';


  constructor(
    private service: AuthService,
    private usuarioService: UsuarioService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.cadastroForm = new FormGroup({
      nome: new FormControl('', Validators.required),
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

  ngOnInit(): void {
    // Captura query params → modo edição
    this.route.queryParams.subscribe(params => {
      console.log('Query Params:', params);
      if (params['id']) {
        this.usuarioId = params['id']; // agora é string
        console.log('Modo edição, id =', this.usuarioId);
        this.tituloFormulario = 'Atualização de Cadastro';
        this.carregarUsuario(this.usuarioId!); // ✅ garante que não é undefined
      } else {
        console.log('Modo cadastro');
      }
    });

  }

  carregarUsuario(id: string) {
    console.log('Carregando usuário com id:', id);
    this.usuarioService.listarUsuarios().subscribe({
      next: (res) => {
        console.log('Lista de usuários recebida:', res);
        const usuario = res.content.find(u => u.id === id); // ✅ compara string
        if (usuario) {
          console.log('Usuário encontrado:', usuario);
          this.cadastroForm.patchValue({
            nome: usuario.nome,
            email: usuario.email,
            roles: usuario.roles.join(','),
            senha: '',
            confirmarSenha: ''
          });
        } else {
          console.warn('Usuário não encontrado na lista!');
        }
      },
      error: (err) => console.error('Erro ao carregar usuário', err)
    });
  }

  salvar() {
    this.cadastroForm.markAllAsTouched();

    // 1️⃣ Log do estado do formulário
    console.log('--- INÍCIO DO SALVAR ---');
    console.log('Formulário válido?', this.cadastroForm.valid);
    console.log('Erros do formulário:', this.cadastroForm.errors);
    console.log('Valores do formulário:', this.cadastroForm.getRawValue());

    if (!this.cadastroForm.valid) {
      console.warn('Formulário inválido. Corrija os campos antes de enviar.');
      return;
    }

    const formValue = this.cadastroForm.getRawValue();

    // 2️⃣ Log das senhas
    console.log('Senha informada:', formValue.senha ? '[OK]' : '[VAZIA]');
    console.log('Confirmar senha:', formValue.confirmarSenha ? '[OK]' : '[VAZIA]');
    if (formValue.senha !== formValue.confirmarSenha) {
      console.warn('As senhas não conferem!');
      return;
    }

    // 3️⃣ Transformar roles em array
    const rolesArray = formValue.roles.split(',').map((r: string) => r.trim());
    console.log('Roles convertidas para array:', rolesArray);

    // 4️⃣ Montar payload final
    const usuarioPayload: any = {
      nome: formValue.nome,
      email: formValue.email,
      roles: rolesArray
    };

    // Só enviar senha se não for vazia (útil para edição)
    if (formValue.senha) {
      usuarioPayload.senha = formValue.senha;
    }

    console.log('Payload final a ser enviado:', usuarioPayload);

    if (this.usuarioId) {
      console.log('Modo ATUALIZAÇÃO, id:', this.usuarioId);
      this.usuarioService.atualizarUsuario(this.usuarioId, usuarioPayload).subscribe({
        next: (res) => {
          console.log('✅ Atualização OK:', res);
          alert('Usuário atualizado com sucesso!');
          this.router.navigate(['/usuarios']);
        },
        error: (err) => {
          console.error('❌ Erro ao atualizar usuário:', err);
        }
      });
    } else {
      console.log('Modo CADASTRO');
      this.service.cadastrar(usuarioPayload).subscribe({
        next: (res) => {
          console.log('✅ Cadastro OK:', res);
          alert('Usuário cadastrado com sucesso!');
          this.router.navigate(['/usuarios']); // ✅ rota para onde você quer ir
        },
        error: (err) => {
          console.error('❌ Erro ao cadastrar usuário:', err);

          const backendError = err.error;

          // ✅ Mensagem geral
          if (backendError?.userMessage) {
            alert(backendError.userMessage);
          }

          // ✅ ERROS DE CAMPOS
          if (backendError?.fields?.length) {
            backendError.fields.forEach((campoErro: any) => {
              const campo = this.cadastroForm.get(campoErro.campo);

              if (campo) {
                campo.setErrors({ backend: campoErro.mensagem });
                campo.markAsTouched();
              }
            });
          }
        }

      });
    }

    console.log('--- FIM DO SALVAR ---');
  }

  isCampoInvalido(nomeCampo: string): boolean {
    const campo = this.cadastroForm.get(nomeCampo);
    return campo?.invalid && campo.touched && campo.errors?.['required'];
  }

  avaliarForcaSenha() {
    const senha = this.cadastroForm.get('senha')?.value || '';

    if (!senha) {
      this.forcaSenha = null;     // ainda não digitou
      this.forcaSenhaTexto = '';
      this.forcaSenhaClasse = '';
      return;
    }

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
