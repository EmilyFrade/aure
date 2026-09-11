import { Component, input } from '@angular/core';

@Component({
  selector: 'app-text-field',
  template: `
    <label class="block">
      @if (label()) {
        <span class="mb-1.5 block text-sm font-medium text-slate-700">
          {{ label() }}
          @if (required()) {
            <span class="text-accent-500">*</span>
          }
        </span>
      }
      <ng-content />
      @if (error()) {
        <span class="mt-1.5 block text-xs text-accent-600">{{ error() }}</span>
      } @else if (hint()) {
        <span class="mt-1.5 block text-xs text-slate-500">{{ hint() }}</span>
      }
    </label>
  `,
  styles: `:host { display: block; }`
})
export class TextField {
  readonly label = input('');
  readonly hint = input('');
  readonly error = input<string | null>(null);
  readonly required = input(false);
}
