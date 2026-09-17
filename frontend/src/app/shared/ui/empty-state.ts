import { Component, input } from '@angular/core';
import { Icon, IconName } from './icon';

@Component({
  selector: 'app-empty-state',
  imports: [Icon],
  template: `
    <div class="flex flex-col items-center justify-center px-6 py-12 text-center">
      <span class="mb-3 flex h-14 w-14 items-center justify-center rounded-2xl bg-primary-50 text-primary-400">
        <app-icon [name]="icon()" [size]="28" />
      </span>
      <p class="font-display text-base font-semibold text-slate-700">{{ title() }}</p>
      @if (description()) {
        <p class="mt-1 max-w-xs text-sm text-slate-500">{{ description() }}</p>
      }
      <div class="mt-4">
        <ng-content />
      </div>
    </div>
  `,
  styles: `:host { display: block; }`
})
export class EmptyState {
  readonly icon = input<IconName>('info');
  readonly title = input.required<string>();
  readonly description = input('');
}
