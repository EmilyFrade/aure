import { Component, HostListener, input, model } from '@angular/core';
import { Icon } from './icon';

@Component({
  selector: 'app-sheet',
  imports: [Icon],
  template: `
    @if (open()) {
      <div class="fixed inset-0 z-50 flex items-end justify-center sm:items-center sm:p-6">
        <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-[2px]" (click)="close()"></div>
        <div
          role="dialog"
          aria-modal="true"
          [attr.aria-label]="title()"
          class="relative flex max-h-[92vh] w-full flex-col rounded-t-3xl bg-white shadow-float sm:max-w-lg sm:rounded-3xl">
          <div class="flex items-center justify-between gap-3 border-b border-primary-100 px-5 py-4">
            <h2 class="font-display text-lg font-semibold text-slate-900">{{ title() }}</h2>
            <button
              type="button"
              class="rounded-full p-1.5 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
              aria-label="Fechar"
              (click)="close()">
              <app-icon name="x" [size]="18" />
            </button>
          </div>
          <div class="overflow-y-auto px-5 py-5">
            <ng-content />
          </div>
        </div>
      </div>
    }
  `
})
export class Sheet {
  readonly open = model(false);
  readonly title = input('');

  @HostListener('document:keydown.escape')
  protected close(): void {
    this.open.set(false);
  }
}
