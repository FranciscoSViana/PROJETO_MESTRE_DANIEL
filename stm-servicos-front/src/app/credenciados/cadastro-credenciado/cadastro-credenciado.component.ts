import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { CredenciadoService } from '../credenciado.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-cadastro-credenciado',
  standalone: false,
  templateUrl: './cadastro-credenciado.component.html',
  styleUrl: './cadastro-credenciado.component.scss'
})
export class CadastroCredenciadoComponent implements OnInit {

  camposForm: FormGroup;
  tipoPessoaSelecionado: number | null = null;

  tiposFluxoPagamento = [
    { label: 'Mensal', value: 'MENSAL' },
    { label: 'Quinzenal', value: 'QUINZENAL' },
    { label: 'Semanal', value: 'SEMANAL' },
    { label: 'Único', value: 'UNICO' },
  ];

  constructor(
    private service: CredenciadoService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.camposForm = new FormGroup({
      id: new FormControl(),
      codigo: new FormControl(),

      rag: new FormControl('', Validators.required),

      // ❌ NÃO obrigatório
      tipoPessoa: new FormControl<number | null>(null),

      // ❌ NÃO obrigatório
      numeroPessoa: new FormControl(''),

      valorChamado: new FormControl(),
      valorKm: new FormControl(),

      tipoFluxoPagamento: new FormControl<string | null>(null),
      chavePix: new FormControl<string | null>(null),

      contato: new FormControl(),
      telefones: new FormControl(),
      email: new FormControl(),

      logradouro: new FormControl<string | null>(null),
      numero: new FormControl<string | null>(null),
      bairro: new FormControl<string | null>(null),
      cidade: new FormControl<string | null>(null),
      estado: new FormControl<string | null>(null),
      complemento: new FormControl<string | null>(null),
      cep: new FormControl<string | null>(null),
    });
  }

  ngOnInit(): void {

    // Observa mudança do tipoPessoa
    this.camposForm.get('tipoPessoa')?.valueChanges.subscribe(value => {
      const tipo = value !== null && value !== undefined ? Number(value) : null;
      this.tipoPessoaSelecionado = tipo;

      this.configurarValidacaoDocumento(tipo);
    });

    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.service.buscarPorId(id).subscribe(credenciado => {

        const tipoConvertido = this.converterTipoPessoa(credenciado.tipoPessoa);

        this.camposForm.patchValue({
          id: credenciado.id,
          codigo: credenciado.codigo,
          rag: credenciado.rag,
          tipoPessoa: tipoConvertido,
          numeroPessoa: credenciado.numeroPessoa,
          valorChamado: credenciado.valorChamado,
          valorKm: credenciado.valorKm,
          tipoFluxoPagamento: credenciado.tipoFluxoPagamento ?? null,
          chavePix:      credenciado.chavePix ?? null,
          contato: credenciado.contato,
          telefones: credenciado.telefones,
          email: credenciado.email,

          cep: credenciado.endereco?.cep,
          logradouro: credenciado.endereco?.logradouro,
          numero: credenciado.endereco?.numero,
          bairro: credenciado.endereco?.bairro,
          cidade: credenciado.endereco?.cidade,
          estado: credenciado.endereco?.estado,
          complemento: credenciado.endereco?.complemento
        });

        this.tipoPessoaSelecionado = tipoConvertido;
        this.configurarValidacaoDocumento(tipoConvertido);
      });
    }
  }

  salvar(): void {
    this.camposForm.markAllAsTouched();

    if (this.camposForm.invalid) {
      console.warn('❌ Formulário inválido:', this.camposForm.errors);
      return;
    }

    const formValue = this.camposForm.getRawValue();

    const parseValor = (value: any): number | null => {
      if (!value) return null;
      if (typeof value === 'number') return value;
      return Number(String(value).replace(/\./g, '').replace(',', '.'));
    };

    const credenciado = {
      rag: formValue.rag,
      tipoPessoa: formValue.tipoPessoa,
      numeroPessoa: formValue.numeroPessoa || null,
      valorChamado: parseValor(formValue.valorChamado),
      valorKm: parseValor(formValue.valorKm),
      tipoFluxoPagamento: formValue.tipoFluxoPagamento || null,
      chavePix:      formValue.chavePix || null,
      contato: formValue.contato,
      telefones: formValue.telefones,
      email: formValue.email,
      endereco: {
        cep: formValue.cep,
        logradouro: formValue.logradouro,
        numero: formValue.numero,
        bairro: formValue.bairro,
        cidade: formValue.cidade,
        estado: formValue.estado,
        complemento: formValue.complemento
      }
    };

    if (formValue.id) {
      this.service.atualizar(formValue.id, credenciado).subscribe(() =>
        this.router.navigate(['/credenciados'])
      );
    } else {
      this.service.salvar(credenciado).subscribe(() =>
        this.router.navigate(['/credenciados'])
      );
    }
  }

  buscarCep() {
    const cep = this.camposForm.get('cep')?.value;
    if (!cep || cep.length < 8) return;

    this.service.buscarCep(cep).subscribe(dados => {
      this.camposForm.patchValue({
        logradouro: dados.logradouro,
        bairro: dados.bairro,
        cidade: dados.localidade,
        estado: dados.uf,
        complemento: dados.complemento
      });
    });
  }

  /**
   * 🔐 Valida SOMENTE se houver valor digitado
   */
  private configurarValidacaoDocumento(tipo: number | null) {
    const ctrl = this.camposForm.get('numeroPessoa');
    if (!ctrl) return;

    ctrl.clearValidators();

    if (tipo === 1) {
      // CPF → valida apenas se preenchido
      ctrl.setValidators(this.documentoComTamanho(11));
    } else if (tipo === 2) {
      // CNPJ → valida apenas se preenchido
      ctrl.setValidators(this.documentoComTamanho(14));
    }

    ctrl.updateValueAndValidity();
  }

  /**
   * ✔️ Valida tamanho somente se houver valor
   */
  private documentoComTamanho(tamanho: number) {
    return (control: AbstractControl) => {
      const valor = control.value;
      if (!valor) return null; // vazio é permitido
      return valor.length === tamanho
        ? null
        : { tamanhoInvalido: true };
    };
  }

  private converterTipoPessoa(tipo: any): number | null {
    if (tipo === null || tipo === undefined) return null;

    if (typeof tipo === 'number') return tipo;

    if (typeof tipo === 'string') {
      const normalizado = tipo.toLowerCase();
      if (normalizado.includes('física')) return 1;
      if (normalizado.includes('jurídica')) return 2;
    }

    return null;
  }
}
