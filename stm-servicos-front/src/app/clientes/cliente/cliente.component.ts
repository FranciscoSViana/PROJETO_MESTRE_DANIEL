import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ClienteService } from '../cliente.service';
import { ActivatedRoute, Router } from '@angular/router';
import { CredenciadoService } from '../../credenciados/credenciado.service';

@Component({
  selector: 'app-cliente',
  standalone: false,
  templateUrl: './cliente.component.html',
  styleUrl: './cliente.component.scss'
})
export class ClienteComponent implements OnInit {

  camposForm: FormGroup;

  constructor(
    private service: ClienteService,
    private credenciadoService: CredenciadoService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.camposForm = new FormGroup({
      id: new FormControl(),
      contrato: new FormControl(),
      nome: new FormControl('', Validators.required),
      valorChamado: new FormControl(),
      valorKm: new FormControl(),
      cnpj: new FormControl(),
      inscricaoEstadual: new FormControl(),
      razaoSocial: new FormControl({ value: '', disabled: true }),

      cep: new FormControl<string | null>(null),
      logradouro: new FormControl<string | null>(null),
      numero: new FormControl<string | null>(null),
      bairro: new FormControl<string | null>(null),
      cidade: new FormControl<string | null>(null),
      estado: new FormControl<string | null>(null),
      complemento: new FormControl<string | null>(null)
    });

    this.camposForm.get('cnpj')?.valueChanges.subscribe(cnpj => {

      const somenteNumeros = cnpj?.replace(/\D/g, '');

      if (somenteNumeros?.length === 14) {
        this.buscarCnpj(somenteNumeros);
      }
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.service.buscarPorId(id).subscribe(cliente => {
        this.camposForm.patchValue(cliente);
      });
    }
  }

  buscarCnpj(cnpj: string) {
    console.log('[DEBUG] Chamando API do backend com CNPJ:', cnpj);

    this.service.consultarCnpj(cnpj).subscribe({
      next: dados => {

        this.camposForm.patchValue({
          razaoSocial: dados.razao_social ?? '',

          cep: dados.cep ?? null,
          logradouro: dados.logradouro ?? null,
          numero: dados.numero ?? null,
          bairro: dados.bairro ?? null,

          // ReceitaWS → seu formulário
          cidade: dados.municipio ?? null,
          estado: dados.uf ?? null,

          complemento: dados.complemento ?? null
        });

      },
      error: err => console.log('Erro ao consultar CNPJ', err)
    });
  }


  salvar() {
    this.camposForm.markAllAsTouched();

    if (this.camposForm.valid) {

      const id = this.camposForm.get('id')?.value;

      const raw = this.camposForm.getRawValue();

      const cliente: any = {
        id: raw.id,
        contrato: raw.contrato,
        nome: raw.nome,
        cnpj: raw.cnpj,
        inscricaoEstadual: raw.inscricaoEstadual,
        razaoSocial: raw.razaoSocial,
        valorChamado: null,
        valorKm: null,

        endereco: {
          cep: raw.cep,
          logradouro: raw.logradouro,
          numero: raw.numero,
          bairro: raw.bairro,
          municipio: raw.cidade, // ✔ converte
          uf: raw.estado,         // ✔ converte
          complemento: raw.complemento
        }
      };

      const parseValor = (value: any): number | null => {
        if (value === null || value === undefined || value === '') return null;
        if (typeof value === 'number') return value;

        let s = String(value).replace(/\./g, '').replace(',', '.');
        const n = Number(s);
        return isNaN(n) ? null : n;
      };

      cliente.valorChamado = parseValor(raw.valorChamado);
      cliente.valorKm = parseValor(raw.valorKm);

      if (id) {
        this.service.atualizar(id, cliente).subscribe({
          next: () => this.router.navigate(['/clientes']),
          error: err => console.error('Erro ao atualizar', err)
        });
      } else {
        this.service.salvar(cliente).subscribe({
          next: () => this.router.navigate(['/clientes']),
          error: err => console.error('Erro ao salvar', err)
        });
      }
    }
  }


  isCampoInvalido(nomeCampo: string): boolean {

    const campo = this.camposForm.get(nomeCampo);

    return campo?.invalid && campo.touched && campo.errors?.['required'];
  }

  buscarCep() {
    const cep = this.camposForm.get('cep')?.value;

    if (!cep || cep.replace(/\D/g, '').length < 8) return;

    this.credenciadoService.buscarCep(cep).subscribe({
      next: dados => {
        this.camposForm.patchValue({
          logradouro: dados.logradouro,
          bairro: dados.bairro,
          cidade: dados.localidade,
          estado: dados.uf,
          complemento: dados.complemento
        });
      },
      error: err => console.error('Erro ao buscar CEP', err)
    });
  }
}
