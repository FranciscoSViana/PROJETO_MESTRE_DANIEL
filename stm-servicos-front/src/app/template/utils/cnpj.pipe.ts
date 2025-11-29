import { Pipe, PipeTransform } from "@angular/core";

@Pipe({
    name: 'cnpj',
    standalone: false
})

export class CnpjPipe implements PipeTransform {
    
    transform(value: string | number | null | undefined): string {
    if (!value) return '';

    let cnpj = value.toString().padStart(14, '0');

    return cnpj.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})$/, "$1.$2.$3/$4-$5");
  }
}