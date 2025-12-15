import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
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

  constructor(private service: CredenciadoService, private router: Router, private route: ActivatedRoute) {
    this.camposForm = new FormGroup({
      id: new FormControl(),
      codigo: new FormControl(),

      rag: new FormControl('', Validators.required),

      tipoPessoa: new FormControl<number | null>(null, {
        nonNullable: false,
        validators: Validators.required
      }),
      numeroPessoa: new FormControl('', Validators.required),

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

    // 🔹 Observa SEMPRE o valor real do form
    this.camposForm.get('tipoPessoa')?.valueChanges.subscribe(value => {
      const tipo = value !== null && value !== undefined
        ? Number(value)
        : null;

      this.tipoPessoaSelecionado = tipo;

      if (tipo !== null) {
        this.configurarValidacaoDocumento(tipo);
      }
    });

    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      console.log('🟡 ID recebido pela rota:', id);

      this.service.buscarPorId(id).subscribe({
        next: credenciado => {

          console.log('🟢 Credenciado bruto vindo da API:', credenciado);
          console.log('🟢 Endereço vindo da API:', credenciado.endereco);

          const tipoConvertido = this.converterTipoPessoa(credenciado.tipoPessoa);

          console.log('➡ tipoPessoa:', credenciado.tipoPessoa);
          console.log('🟣 tipoPessoa convertido:', tipoConvertido);

          // 1️⃣ Patch geral
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

            // Endereço
            cep: credenciado.endereco?.cep,
            logradouro: credenciado.endereco?.logradouro,
            numero: credenciado.endereco?.numero,
            bairro: credenciado.endereco?.bairro,
            cidade: credenciado.endereco?.cidade,
            estado: credenciado.endereco?.estado,
            complemento: credenciado.endereco?.complemento
          });

          // 2️⃣ Atualiza controle visual
          this.tipoPessoaSelecionado = tipoConvertido;

          // 3️⃣ Aplica validação correta
          if (tipoConvertido !== null) {
            this.configurarValidacaoDocumento(tipoConvertido);
          }
        }
      });
    }
  }


  salvar(): void {
    this.camposForm.markAllAsTouched();

    if (this.camposForm.invalid) {
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
      numeroPessoa: formValue.numeroPessoa,
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

    if (formValue.id) {
      this.service.atualizar(formValue.id, credenciado).subscribe({
        next: () => this.router.navigate(['/credenciados'])
      });
    } else {
      this.service.salvar(credenciado).subscribe({
        next: () => this.router.navigate(['/credenciados'])
      });
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

  private configurarValidacaoDocumento(tipo: number) {
    const ctrl = this.camposForm.get('numeroPessoa');

    if (!ctrl) return;

    ctrl.clearValidators();

    if (tipo === 1) {
      // CPF → 11 dígitos
      ctrl.setValidators([
        Validators.required,
        Validators.minLength(11)
      ]);
    } else if (tipo === 2) {
      // CNPJ → 14 dígitos
      ctrl.setValidators([
        Validators.required,
        Validators.minLength(14)
      ]);
    }

    ctrl.updateValueAndValidity();
  }

  private converterTipoPessoa(tipo: any): number | null {
    if (tipo === null || tipo === undefined) return null;

    // Já veio número
    if (typeof tipo === 'number') {
      return tipo;
    }

    // Veio string (caso atual)
    if (typeof tipo === 'string') {
      const normalizado = tipo.toLowerCase();

      if (normalizado.includes('física')) return 1;
      if (normalizado.includes('jurídica')) return 2;
    }

    return null;
  }
}
