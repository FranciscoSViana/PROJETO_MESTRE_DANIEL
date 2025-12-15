import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CadastroTecnicoComponent } from './cadastro-tecnico.component';

describe('CadastroTecnicoComponent', () => {
  let component: CadastroTecnicoComponent;
  let fixture: ComponentFixture<CadastroTecnicoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CadastroTecnicoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CadastroTecnicoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
