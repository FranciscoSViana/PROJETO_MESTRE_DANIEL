import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { AuthService } from '../../auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { UsuarioService } from '../usuario.service';
import { debounceTime, distinctUntilChanged, switchMap, catchError, of } from 'rxjs';

@Component({
  selector: 'app-cadastro-usuario',
  standalone: false,
  templateUrl: './cadastro-usuario.component.html',
  styleUrl: './cadastro-usuario.component.scss'
})
export class CadastroUsuarioComponent implements OnInit {

  cadastroForm!: FormGroup;
  rolesDisponiveis = ['ADMIN', 'USER'];
  usuarioId?: string;

  forcaSenha: number | null = null;
  forcaSenhaTexto = '';
  forcaSenhaClasse = '';

  usernamePreview = '';
  carregandoUsername = false;

  private SENHA_FORTE = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;

  constructor(
    private authService: AuthService,
    private usuarioService: UsuarioService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.cadastroForm = new FormGroup(
      {
        nomeCompleto: new FormControl('', [Validators.required, Validators.minLength(3)]),
        dataNascimento: new FormControl('', [Validators.required, this.validarIdadeMinima]),
        email: new FormControl('', [Validators.required, Validators.email]),
        senha: new FormControl('', [
          Validators.required,
          Validators.minLength(8),
          Validators.pattern(this.SENHA_FORTE)
        ]),
        confirmarSenha: new FormControl('', Validators.required),
        roles: new FormControl('USER', Validators.required)
      },
      { validators: this.senhasConferem }
    );

    // Preview de username consultando o backend
    this.cadastroForm.get('nomeCompleto')!.valueChanges.pipe(
      debounceTime(600),
      distinctUntilChanged(),
      switchMap(nome => {
        if (!nome || nome.trim().length < 3 || this.usuarioId) {
          this.usernamePreview = '';
          return of(null);
        }
        this.carregandoUsername = true;
        return this.authService.previewUsername(nome).pipe(
          catchError(() => of(null))
        );
      })
    ).subscribe(res => {
      this.carregandoUsername = false;
      this.usernamePreview = res?.username ?? '';
    });

    this.cadastroForm.get('senha')!.valueChanges
      .subscribe(v => this.avaliarForcaSenha(v || ''));

    this.route.queryParams.subscribe(params => {
      if (params['id']) {
        this.usuarioId = params['id'];
        this.carregarUsuario(this.usuarioId!);
      }
    });
  }

  // ─── VALIDADORES ─────────────────────────────

  private senhasConferem(group: AbstractControl): ValidationErrors | null {
    const senha = group.get('senha')?.value;
    const confirmar = group.get('confirmarSenha')?.value;
    if (senha && confirmar && senha !== confirmar) {
      group.get('confirmarSenha')?.setErrors({ naoConfere: true });
      return { naoConfere: true };
    }
    if (group.get('confirmarSenha')?.hasError('naoConfere')) {
      group.get('confirmarSenha')?.setErrors(null);
    }
    return null;
  }

  private validarIdadeMinima(control: AbstractControl): ValidationErrors | null {
    if (!control.value) return null;
    const nascimento = new Date(control.value);
    const hoje = new Date();
    let idade = hoje.getFullYear() - nascimento.getFullYear();
    const m = hoje.getMonth() - nascimento.getMonth();
    if (m < 0 || (m === 0 && hoje.getDate() < nascimento.getDate())) idade--;
    if (idade < 18) return { menorDeIdade: true };
    if (idade > 120) return { dataInvalida: true };
    return null;
  }

  // ─── FORÇA DA SENHA ──────────────────────────

  avaliarForcaSenha(senha: string) {
    if (!senha) { this.forcaSenha = null; this.forcaSenhaTexto = ''; return; }

    let score = 0;
    if (senha.length >= 8) score += 25;
    if (/[A-Z]/.test(senha)) score += 25;
    if (/[0-9]/.test(senha)) score += 25;
    if (/[^A-Za-z0-9]/.test(senha)) score += 25;

    this.forcaSenha = score;

    if (score <= 25)      { this.forcaSenhaTexto = 'Senha fraca';  this.forcaSenhaClasse = 'bg-red-500'; }
    else if (score <= 50) { this.forcaSenhaTexto = 'Senha média';  this.forcaSenhaClasse = 'bg-yellow-500'; }
    else if (score <= 75) { this.forcaSenhaTexto = 'Senha boa';    this.forcaSenhaClasse = 'bg-blue-500'; }
    else                  { this.forcaSenhaTexto = 'Senha forte';  this.forcaSenhaClasse = 'bg-green-500'; }
  }

  bloquearColar(event: ClipboardEvent) {
    const texto = event.clipboardData?.getData('text') || '';
    if (!this.SENHA_FORTE.test(texto)) {
      event.preventDefault();
      alert('⚠️ Não é permitido colar uma senha que não atenda aos requisitos de segurança.');
    }
  }

  // ─── GETTERS PARA REQUISITOS DE SENHA ────────

  get senhaAtual(): string         { return this.cadastroForm?.get('senha')?.value ?? ''; }
  get senhaTemMinimo(): boolean    { return this.senhaAtual.length >= 8; }
  get senhaTemMaiuscula(): boolean { return /[A-Z]/.test(this.senhaAtual); }
  get senhaTemMinuscula(): boolean { return /[a-z]/.test(this.senhaAtual); }
  get senhaTemNumero(): boolean    { return /[0-9]/.test(this.senhaAtual); }
  get senhaTemEspecial(): boolean  { return /[^A-Za-z0-9]/.test(this.senhaAtual); }

  // ─── CARREGAR PARA EDIÇÃO ────────────────────

  carregarUsuario(id: string) {
    this.usuarioService.listarUsuarios().subscribe({
      next: (res) => {
        const usuario = res.content.find((u: any) => u.id === id);
        if (usuario) {
          this.cadastroForm.patchValue({
            nomeCompleto: usuario.nomeCompleto,
            email: usuario.email,
            dataNascimento: usuario.dataNascimento,
            roles: usuario.roles?.[0] ?? 'USER',
            senha: '',
            confirmarSenha: ''
          });
          this.usernamePreview = usuario.username ?? '';

          this.cadastroForm.get('senha')?.clearValidators();
          this.cadastroForm.get('confirmarSenha')?.clearValidators();
          this.cadastroForm.get('senha')?.updateValueAndValidity();
          this.cadastroForm.get('confirmarSenha')?.updateValueAndValidity();
        }
      },
      error: err => console.error('Erro ao carregar usuário', err)
    });
  }

  // ─── SALVAR ──────────────────────────────────

  salvar() {
    this.cadastroForm.markAllAsTouched();
    if (!this.cadastroForm.valid) return;

    const v = this.cadastroForm.getRawValue();
    const rolesArray = typeof v.roles === 'string'
      ? v.roles.split(',').map((r: string) => r.trim())
      : [v.roles];

    const payload: any = {
      nomeCompleto: v.nomeCompleto,
      dataNascimento: v.dataNascimento,
      email: v.email,
      roles: rolesArray
    };

    if (v.senha) payload.senha = v.senha;

    if (this.usuarioId) {
      this.usuarioService.atualizarUsuario(this.usuarioId, payload).subscribe({
        next: () => { alert('Usuário atualizado com sucesso!'); this.router.navigate(['/usuarios']); },
        error: err => alert(err.error?.error ?? 'Erro ao atualizar usuário.')
      });
    } else {
      this.authService.cadastrar(payload).subscribe({
        next: () => { alert('Usuário cadastrado com sucesso!'); this.router.navigate(['/usuarios']); },
        error: err => alert(err.error?.error ?? 'Erro ao cadastrar usuário.')
      });
    }
  }

  // ─── HELPERS ─────────────────────────────────

  /**
   * Retorna true apenas se o campo for inválido E já tiver sido tocado.
   * Usado para controlar bordas vermelhas nos inputs.
   */
  isCampoInvalido(campo: string): boolean {
    const c = this.cadastroForm.get(campo);
    return !!(c?.invalid && c.touched);
  }

  /**
   * Retorna true apenas se o campo tiver o erro específico E já tiver sido tocado.
   * Usado para exibir mensagens de erro individuais.
   */
  erroCampo(campo: string, erro: string): boolean {
    const c = this.cadastroForm.get(campo);
    return !!(c?.touched && c.hasError(erro));
  }

  calcularIdade(dataNascimento: string): number | null {
    if (!dataNascimento) return null;
    const nascimento = new Date(dataNascimento);
    const hoje = new Date();
    let idade = hoje.getFullYear() - nascimento.getFullYear();
    const m = hoje.getMonth() - nascimento.getMonth();
    if (m < 0 || (m === 0 && hoje.getDate() < nascimento.getDate())) idade--;
    return idade;
  }
}