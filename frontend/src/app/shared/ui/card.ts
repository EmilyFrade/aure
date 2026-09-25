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
    ['rounded-2xl bg-white shadow-card ring-1 ring-primary-100/70', this.padded() ? 'p-5' : '']
      .filter(Boolean)
      .join(' ')
  );
}
