import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Icon } from '../ui/icon';

@Component({
  selector: 'app-public-shell',
  imports: [RouterLink, Icon],
  template: `
    <div class="min-h-screen">
      <header class="sticky top-0 z-20 border-b border-slate-100 bg-white/90 backdrop-blur">
        <div class="mx-auto flex h-16 max-w-5xl items-center justify-between px-4 sm:px-6">
          <a routerLink="/" class="font-display text-2xl font-bold text-primary-500">{{ brand() }}</a>
          @if (showAuthActions()) {
            <div class="flex items-center gap-1.5">
              <a
                routerLink="/meus-agendamentos"
                class="inline-flex items-center gap-1.5 rounded-xl px-2.5 py-1.5 text-sm font-medium text-primary-600 transition hover:bg-primary-50 sm:text-slate-600 sm:hover:bg-slate-100">
                <app-icon name="calendar" [size]="18" />
                Meus agendamentos
              </a>
              <a
                routerLink="/profissional/login"
                class="hidden rounded-xl border border-primary-200 px-3 py-1.5 text-sm font-medium text-primary-600 transition hover:bg-primary-50 sm:inline-block">
                Sou profissional
              </a>
            </div>
          }
        </div>
      </header>

      <main class="mx-auto max-w-5xl px-4 py-6 sm:px-6">
        <ng-content />
      </main>
    </div>
  `
})
export class PublicShell {
  readonly brand = input('Aure');
  readonly showAuthActions = input(true);
}
