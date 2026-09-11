import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-public-shell',
  imports: [RouterLink],
  template: `
    <div class="min-h-screen">
      <header class="sticky top-0 z-20 border-b border-slate-100 bg-white/90 backdrop-blur">
        <div class="mx-auto flex h-16 max-w-5xl items-center justify-between px-4 sm:px-6">
          <a routerLink="/" class="font-display text-2xl font-bold text-primary-500">{{ brand() }}</a>
          @if (showAuthActions()) {
            <div class="flex items-center gap-1.5">
              <a
                routerLink="/profissional/login"
                class="rounded-xl px-3 py-1.5 text-sm font-medium text-slate-600 transition hover:bg-slate-100">
                Entrar
              </a>
              <a
                routerLink="/profissional/cadastro"
                class="rounded-xl bg-primary-500 px-3 py-1.5 text-sm font-medium text-white shadow-sm transition hover:bg-primary-600">
                Cadastrar
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
