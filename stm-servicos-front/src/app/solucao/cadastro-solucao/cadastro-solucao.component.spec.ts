import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CadastroSolucaoComponent } from './cadastro-solucao.component';

describe('CadastroSolucaoComponent', () => {
  let component: CadastroSolucaoComponent;
  let fixture: ComponentFixture<CadastroSolucaoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CadastroSolucaoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CadastroSolucaoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
