import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { CredenciadoService } from '../credenciado.service';

@Component({
  selector: 'app-cadastro-credenciado',
  standalone: false,
  templateUrl: './cadastro-credenciado.component.html',
  styleUrl: './cadastro-credenciado.component.scss'
})
export class CadastroCredenciadoComponent implements OnInit {

  camposForm: FormGroup;
  ufs: any[] = [];
  cidades: any[] = [];

  constructor(private service: CredenciadoService) {
    this.camposForm = new FormGroup({
      id: new FormControl(),
      codigo: new FormControl(),
      rag: new FormControl(),
      cidade: new FormControl(),
      uf: new FormControl(),
      tipo: new FormControl(),
      valorChamado: new FormControl(),
      valorKm: new FormControl(),
      quantidadeOSAtendidas: new FormControl(),
      contato: new FormControl(),
      telefones: new FormControl(),
      email: new FormControl(),
      tecnico: new FormControl(),
      cpf: new FormControl(),
      base: new FormControl()
    });
  }

  ngOnInit(): void {
    this.carregarEstados();

    // Atualiza as cidades quando UF muda
    this.camposForm.get('uf')?.valueChanges.subscribe(uf => {
      if (uf) {
        this.carregarCidades(uf);
      } else {
        this.cidades = [];
        this.camposForm.get('cidade')?.setValue('');
      }
    });
  }

  carregarEstados() {
    this.service.listarEstados().subscribe({
      next: (estados) => this.ufs = estados,
      error: (err) => console.error('Erro ao carregar estados', err)
    });
  }

  carregarCidades(uf: string) {
    this.service.listarMunicipios(uf).subscribe({
      next: (municipios) => this.cidades = municipios,
      error: (err) => console.error('Erro ao carregar municípios', err)
    });
  }

  isCampoInvalido(campo: string): boolean {
    const controle = this.camposForm.get(campo);
    return controle ? controle.invalid && (controle.dirty || controle.touched) : false;
  }

  salvar() {
    if (this.camposForm.valid) {
      // Remove id e codigo do envio
      const credenciado = { ...this.camposForm.value };

      this.service.salvar(credenciado).subscribe({
        next: () => alert('Credenciado salvo com sucesso!'),
        error: (err) => console.error('Erro ao salvar credenciado', err)
      });
    } else {
      this.camposForm.markAllAsTouched();
    }
  }
}
