import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'preserveLineBreaks',
  standalone: true
})
export class PreserveLineBreaksPipe implements PipeTransform {
  transform(value: string): string {
    if (!value) return '';

    // Reemplazar saltos de línea con <br> tags
    return value.replace(/\n/g, '<br>');
  }
}
