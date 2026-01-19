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

  constructor(
    private service: CredenciadoService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    console.log('🟢 Constructor CadastroCredenciadoComponent');

    this.camposForm = new FormGroup({
      id: new FormControl(),
      codigo: new FormControl(),

      rag: new FormControl('', Validators.required),

      tipoPessoa: new FormControl<number | null>(null),
      numeroPessoa: new FormControl(''),

      valorChamado: new FormControl(),
      valorKm: new FormControl(),
      quantidadeOSAtendidas: new FormControl(),

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
    console.log('🟢 ngOnInit iniciado');

    // Observa mudança do tipoPessoa
    this.camposForm.get('tipoPessoa')?.valueChanges.subscribe(value => {
      const tipo = value !== null && value !== undefined ? Number(value) : null;

      console.log('🔄 tipoPessoa alterado:', value, '→ convertido:', tipo);

      this.tipoPessoaSelecionado = tipo;
      this.configurarValidacaoDocumento(tipo);
    });

    const id = this.route.snapshot.paramMap.get('id');
    console.log('🧭 ID da rota:', id);

    if (id) {
      console.log('📥 Modo edição - buscando credenciado por ID');

      this.service.buscarPorId(id).subscribe({
        next: credenciado => {
          console.log('✅ Credenciado recebido da API:', credenciado);

          const tipoConvertido = this.converterTipoPessoa(credenciado.tipoPessoa);
          console.log('🔁 tipoPessoa convertido:', tipoConvertido);

          this.camposForm.patchValue({
            id: credenciado.id,
            codigo: credenciado.codigo,
            rag: credenciado.rag,
            tipoPessoa: tipoConvertido,
            numeroPessoa: credenciado.numeroPessoa,
            valorChamado: credenciado.valorChamado,
            valorKm: credenciado.valorKm,
            quantidadeOSAtendidas: credenciado.quantidadeOSAtendidas,
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
        },
        error: err => {
          console.error('❌ Erro ao buscar credenciado:', err);
        }
      });
    }
  }

  salvar(): void {
    console.log('💾 Ação salvar acionada');

    this.camposForm.markAllAsTouched();

    if (this.camposForm.invalid) {
      console.warn('❌ Formulário inválido');
      console.log('📋 Status dos campos:', this.camposForm.controls);
      return;
    }

    const formValue = this.camposForm.getRawValue();
    console.log('📄 Valores do formulário:', formValue);

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
      quantidadeOSAtendidas: formValue.quantidadeOSAtendidas,
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

    console.log('📦 Payload enviado ao backend:', credenciado);

    if (formValue.id) {
      console.log('✏️ Atualizando credenciado ID:', formValue.id);

      this.service.atualizar(formValue.id, credenciado).subscribe({
        next: () => {
          console.log('✅ Credenciado atualizado com sucesso');
          this.router.navigate(['/credenciados']);
        },
        error: err => {
          console.error('❌ Erro ao atualizar:', err);
        }
      });
    } else {
      console.log('➕ Criando novo credenciado');

      this.service.salvar(credenciado).subscribe({
        next: () => {
          console.log('✅ Credenciado salvo com sucesso');
          this.router.navigate(['/credenciados']);
        },
        error: err => {
          console.error('❌ Erro ao salvar:', err);
        }
      });
    }
  }

  buscarCep() {
    const cep = this.camposForm.get('cep')?.value;
    console.log('📮 Buscar CEP:', cep);

    if (!cep || cep.length < 8) {
      console.warn('⚠️ CEP inválido ou incompleto');
      return;
    }

    this.service.buscarCep(cep).subscribe({
      next: dados => {
        console.log('📬 Dados do CEP recebidos:', dados);

        this.camposForm.patchValue({
          logradouro: dados.logradouro,
          bairro: dados.bairro,
          cidade: dados.localidade,
          estado: dados.uf,
          complemento: dados.complemento
        });
      },
      error: err => {
        console.error('❌ Erro ao buscar CEP:', err);
      }
    });
  }

  private configurarValidacaoDocumento(tipo: number | null) {
    console.log('🔐 Configurando validação documento. Tipo:', tipo);

    const ctrl = this.camposForm.get('numeroPessoa');
    if (!ctrl) return;

    ctrl.clearValidators();

    if (tipo === 1) {
      console.log('📄 Validação CPF (11)');
      ctrl.setValidators(this.documentoComTamanho(11));
    } else if (tipo === 2) {
      console.log('🏢 Validação CNPJ (14)');
      ctrl.setValidators(this.documentoComTamanho(14));
    }

    ctrl.updateValueAndValidity();
  }

  private documentoComTamanho(tamanho: number) {
    return (control: AbstractControl) => {
      const valor = control.value;
      if (!valor) return null;
      return valor.length === tamanho ? null : { tamanhoInvalido: true };
    };
  }

  private converterTipoPessoa(tipo: any): number | null {
    console.log('🔁 Converter tipoPessoa:', tipo);

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
