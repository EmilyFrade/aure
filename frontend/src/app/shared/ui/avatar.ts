import { Component, computed, input, signal } from '@angular/core';

@Component({
  selector: 'app-avatar',
  template: `
    @if (showPhoto()) {
      <img [src]="photoUrl()" [alt]="name()" class="h-full w-full object-cover" (error)="failedUrl.set(photoUrl() ?? null)" />
    } @else {
      <span aria-hidden="true">{{ initials() }}</span>
    }
  `,
  host: {
    '[class]': 'classes()',
    '[style.width.px]': 'size()',
    '[style.height.px]': 'size()',
    '[style.font-size.px]': 'size() * 0.36',
    '[attr.title]': 'name()'
  }
})
export class Avatar {
  readonly name = input('');
  readonly photoUrl = input<string | null | undefined>(null);
  readonly size = input(40);
  readonly shape = input<'circle' | 'rounded'>('circle');

  protected readonly failedUrl = signal<string | null>(null);

  protected readonly showPhoto = computed(() => !!this.photoUrl() && this.photoUrl() !== this.failedUrl());

  protected readonly initials = computed(() =>
    this.name()
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]?.toUpperCase() ?? '')
      .join('')
  );

  protected readonly classes = computed(
    () =>
      'inline-flex shrink-0 select-none items-center justify-center overflow-hidden bg-primary-100 ' +
      'font-display font-semibold text-primary-600 ' +
      (this.shape() === 'circle' ? 'rounded-full' : 'rounded-2xl')
  );
}
