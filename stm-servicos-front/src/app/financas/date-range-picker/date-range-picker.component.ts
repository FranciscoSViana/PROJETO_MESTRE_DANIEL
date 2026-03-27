import { Component, ElementRef, EventEmitter, HostListener, Input, OnChanges, Output } from '@angular/core';

@Component({
  selector: 'app-date-range-picker',
  standalone: false,
  templateUrl: './date-range-picker.component.html',
  styleUrl: './date-range-picker.component.scss'
})
export class DateRangePickerComponent implements OnChanges {

  @Input() titulo = 'Período';
  @Input() inicioValue = '';
  @Input() fimValue = '';

  @Output() inicioChange = new EventEmitter<string>();
  @Output() fimChange = new EventEmitter<string>();

  aberto = false;
  inicio = '';
  fim = '';

  constructor(private el: ElementRef) { }

  ngOnChanges(): void {
    this.inicio = this.inicioValue;
    this.fim = this.fimValue;
  }

  get temFiltro(): boolean {
    return !!(this.inicio || this.fim);
  }

  get label(): string {
    if (!this.inicio && !this.fim) return 'Qualquer data';
    if (this.inicio && !this.fim) return `De ${this.fmt(this.inicio)}`;
    if (!this.inicio && this.fim) return `Até ${this.fmt(this.fim)}`;
    return `${this.fmt(this.inicio)} – ${this.fmt(this.fim)}`;
  }

  toggle(): void { this.aberto = !this.aberto; }

  emitir(): void {
    this.inicioChange.emit(this.inicio);
    this.fimChange.emit(this.fim);
  }

  limpar(event?: Event): void {
    event?.stopPropagation();
    this.inicio = '';
    this.fim = '';
    this.emitir();
    this.aberto = false;
  }

  @HostListener('document:click', ['$event'])
  onDocClick(e: MouseEvent): void {
    if (!this.el.nativeElement.contains(e.target)) {
      this.aberto = false;
    }
  }

  private fmt(d: string): string {
    if (!d) return '';
    const [y, m, day] = d.split('-');
    return `${day}/${m}/${y}`;
  }
}
