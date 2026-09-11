import { Component, computed, input } from '@angular/core';

type SpinnerSize = 'sm' | 'md' | 'lg';

@Component({
  selector: 'app-spinner',
  template: `<span [class]="classes()" role="status" aria-label="Carregando"></span>`,
  styles: `
    :host { display: inline-flex; }
    span {
      display: inline-block;
      border-radius: 9999px;
      border-style: solid;
      border-color: currentColor;
      border-right-color: transparent;
      animation: spin 0.6s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }
  `
})
export class Spinner {
  readonly size = input<SpinnerSize>('md');

  protected readonly classes = computed(() => {
    const sizes: Record<SpinnerSize, string> = {
      sm: 'h-4 w-4 border-2',
      md: 'h-6 w-6 border-2',
      lg: 'h-9 w-9 border-[3px]'
    };
    return sizes[this.size()];
  });
}
