import { Component, computed, input } from '@angular/core';
import { Spinner } from './spinner';

type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger';
type ButtonSize = 'sm' | 'md' | 'lg';

@Component({
  selector: 'app-button',
  imports: [Spinner],
  template: `
    <button [type]="type()" [disabled]="disabled() || loading()" [class]="classes()">
      @if (loading()) {
        <app-spinner size="sm" />
      }
      <ng-content />
    </button>
  `,
  styles: `:host { display: contents; }`
})
export class Button {
  readonly type = input<'button' | 'submit'>('button');
  readonly variant = input<ButtonVariant>('primary');
  readonly size = input<ButtonSize>('md');
  readonly disabled = input(false);
  readonly loading = input(false);
  readonly full = input(false);

  protected readonly classes = computed(() => {
    const base =
      'inline-flex items-center justify-center gap-2 rounded-xl font-medium transition ' +
      'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-200 ' +
      'disabled:cursor-not-allowed disabled:opacity-60';

    const sizes: Record<ButtonSize, string> = {
      sm: 'px-3 py-1.5 text-sm',
      md: 'px-4 py-2.5 text-sm',
      lg: 'px-5 py-3 text-base'
    };

    const variants: Record<ButtonVariant, string> = {
      primary: 'bg-primary-500 text-white shadow-sm hover:bg-primary-600 active:bg-primary-700',
      secondary: 'bg-primary-100 text-primary-700 hover:bg-primary-200',
      outline: 'border border-slate-200 bg-white text-slate-700 hover:bg-slate-50',
      ghost: 'text-slate-600 hover:bg-slate-100',
      danger: 'border border-accent-200 bg-white text-accent-600 hover:bg-accent-50'
    };

    return [base, sizes[this.size()], variants[this.variant()], this.full() ? 'w-full' : '']
      .filter(Boolean)
      .join(' ');
  });
}
