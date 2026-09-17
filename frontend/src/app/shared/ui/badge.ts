import { Component, computed, input } from '@angular/core';

export type BadgeVariant = 'primary' | 'success' | 'warning' | 'danger' | 'neutral';

@Component({
  selector: 'app-badge',
  template: `<ng-content />`,
  styles: `:host { display: inline-flex; }`,
  host: { '[class]': 'classes()' }
})
export class Badge {
  readonly variant = input<BadgeVariant>('neutral');

  protected readonly classes = computed(() => {
    const base = 'items-center rounded-full px-2.5 py-0.5 text-xs font-medium';
    const variants: Record<BadgeVariant, string> = {
      primary: 'bg-primary-100 text-primary-700',
      success: 'bg-emerald-100 text-emerald-700',
      warning: 'bg-accent-100 text-accent-600',
      danger: 'bg-red-100 text-red-700',
      neutral: 'bg-slate-100 text-slate-600'
    };
    return `${base} ${variants[this.variant()]}`;
  });
}

export function badgeForStatus(status: string): BadgeVariant {
  switch (status) {
    case 'CONFIRMED':
      return 'primary';
    case 'COMPLETED':
      return 'success';
    case 'PENDING':
      return 'warning';
    case 'CANCELLED':
      return 'neutral';
    case 'NO_SHOW':
      return 'danger';
    default:
      return 'neutral';
  }
}
