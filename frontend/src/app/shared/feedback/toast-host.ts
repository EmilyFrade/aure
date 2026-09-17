import { Component, inject } from '@angular/core';
import { Icon, IconName } from '../ui/icon';
import { ToastKind, ToastService } from './toast.service';

@Component({
  selector: 'app-toast-host',
  imports: [Icon],
  template: `
    <div class="pointer-events-none fixed inset-x-0 top-0 z-50 flex flex-col items-center gap-2 p-4 sm:items-end">
      @for (toast of toasts(); track toast.id) {
        <div
          class="pointer-events-auto flex w-full max-w-sm items-start gap-3 rounded-xl border bg-white px-4 py-3 shadow-lg"
          [class]="border(toast.kind)"
          role="status">
          <span class="mt-0.5" [class]="color(toast.kind)">
            <app-icon [name]="icon(toast.kind)" [size]="18" />
          </span>
          <p class="flex-1 text-sm text-slate-700">{{ toast.text }}</p>
          <button
            type="button"
            class="text-slate-400 transition hover:text-slate-600"
            aria-label="Fechar"
            (click)="toastService.dismiss(toast.id)">
            <app-icon name="x" [size]="16" />
          </button>
        </div>
      }
    </div>
  `
})
export class ToastHost {
  protected readonly toastService = inject(ToastService);
  protected readonly toasts = this.toastService.toasts;

  protected icon(kind: ToastKind): IconName {
    return kind === 'success' ? 'check' : kind === 'error' ? 'info' : 'info';
  }

  protected color(kind: ToastKind): string {
    return kind === 'success'
      ? 'text-emerald-500'
      : kind === 'error'
        ? 'text-accent-500'
        : 'text-primary-500';
  }

  protected border(kind: ToastKind): string {
    return kind === 'success'
      ? 'border-emerald-100'
      : kind === 'error'
        ? 'border-accent-100'
        : 'border-primary-100';
  }
}
