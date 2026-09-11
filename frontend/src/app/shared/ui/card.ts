import { Component, computed, input } from '@angular/core';

@Component({
  selector: 'app-card',
  template: `<ng-content />`,
  styles: `:host { display: block; }`,
  host: { '[class]': 'classes()' }
})
export class Card {
  readonly padded = input(true);

  protected readonly classes = computed(() =>
    ['rounded-2xl border border-slate-100 bg-white shadow-sm', this.padded() ? 'p-5' : '']
      .filter(Boolean)
      .join(' ')
  );
}
