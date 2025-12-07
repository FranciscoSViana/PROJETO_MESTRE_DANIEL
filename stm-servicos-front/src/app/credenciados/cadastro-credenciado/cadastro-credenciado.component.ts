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
  ufs: any[] = [];
  cidades: any[] = [];

  constructor(private service: CredenciadoService, private router: Router, private route: ActivatedRoute) {
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
      cpf: new FormControl('', Validators.required),
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

    // Se vier um ID na rota, carregar o credenciado para edição
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.service.buscarPorId(id).subscribe({
        next: credenciado => {
          this.camposForm.patchValue(credenciado);
          if (credenciado.uf) {
            this.carregarCidades(credenciado.uf);
          }
        },
        error: err => console.error('Erro ao carregar credenciado', err)
      });
    }
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
    console.log('Salvar chamado');
    this.camposForm.markAllAsTouched();

    if (this.camposForm.valid) {

      const id = this.camposForm.get('id')?.value;
      const credenciado = this.camposForm.getRawValue();
      
      const parseValor = (value: any): number | null => {
        if (value === null || value === undefined || value === '') return null;

        // Se já for number, apenas retorna
        if (typeof value === 'number') return value;

        // Se for string formatada: "1.000,50"
        let s = String(value);
        s = s.replace(/\./g, '');  // remove separador de milhar
        s = s.replace(',', '.');  // troca vírgula por ponto

        const n = Number(s);
        return isNaN(n) ? null : n;
      };

      credenciado.valorChamado = parseValor(credenciado.valorChamado);
      credenciado.valorKm = parseValor(credenciado.valorKm);

      if (id) {
        // EDITAR
        this.service.atualizar(id, credenciado).subscribe({
          next: () => {
            this.router.navigate(['/credenciados']);
          },
          error: err => {
            console.error('Erro ao atualizar credenciado', err);
            alert('Erro ao atualizar credenciado.');
          }
        });

      } else {
        // CRIAR
        this.service.salvar(credenciado).subscribe({
          next: () => {
            this.router.navigate(['/credenciados']);
          },
          error: err => {
            console.error('Erro ao salvar credenciado', err);
            const mensagem = err?.error?.userMessage || 'Erro ao salvar credenciado';
            alert(mensagem);
          }
        });
      }
    }
  }
}
