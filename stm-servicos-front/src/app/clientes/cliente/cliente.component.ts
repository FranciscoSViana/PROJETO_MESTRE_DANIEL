import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ClienteService } from '../cliente.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-cliente',
  standalone: false,
  templateUrl: './cliente.component.html',
  styleUrl: './cliente.component.scss'
})
export class ClienteComponent implements OnInit {

  camposForm: FormGroup;

  constructor(private service: ClienteService, private router: Router, private route: ActivatedRoute) {
    this.camposForm = new FormGroup({
      id: new FormControl(),
      contrato: new FormControl(),
      nome: new FormControl('', Validators.required),
      valorChamado: new FormControl(),
      valorKm: new FormControl(),
      cnpj: new FormControl(),
      inscricaoEstadual: new FormControl(),
      razaoSocial: new FormControl({ value: '', disabled: true })
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
        this.camposForm.get('razaoSocial')?.setValue(dados.razao_social ?? '')
      },
      error: err => console.log('Erro ao consultar CNPJ', err)
    });
  }

  salvar() {
    this.camposForm.markAllAsTouched();

    if (this.camposForm.valid) {

      const id = this.camposForm.get('id')?.value;
      const cliente = this.camposForm.getRawValue();

      if (id) {
        // EDITAR
        this.service.atualizar(id, cliente).subscribe({
          next: () => {
            this.router.navigate(['/clientes']);
          }
        });
      } else {
        // CRIAR
        this.service.salvar(cliente).subscribe({
          next: () => {
            this.router.navigate(['/clientes']);
          }
        });
      }
    }
  }


  isCampoInvalido(nomeCampo: string): boolean {

    const campo = this.camposForm.get(nomeCampo);

    return campo?.invalid && campo.touched && campo.errors?.['required'];
  }
}
