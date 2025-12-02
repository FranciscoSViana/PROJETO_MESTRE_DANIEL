import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsultaFaturamentoComponent } from './consulta-faturamento.component';

describe('ConsultaFaturamentoComponent', () => {
  let component: ConsultaFaturamentoComponent;
  let fixture: ComponentFixture<ConsultaFaturamentoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ConsultaFaturamentoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConsultaFaturamentoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
