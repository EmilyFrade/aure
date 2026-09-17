import { Component, input, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { Icon, IconName } from '../ui/icon';

export interface ShellLink {
  readonly label: string;
  readonly icon: IconName;
  readonly path?: string;
}

export const PROFESSIONAL_NAV: readonly ShellLink[] = [
  { label: 'Painel Semanal', icon: 'calendar-week', path: '/profissional/agenda' },
  { label: 'Agenda Diária', icon: 'clock' },
  { label: 'Lista de Espera', icon: 'users' },
  { label: 'Configurações', icon: 'settings', path: '/profissional/configuracoes' }
];

@Component({
  selector: 'app-shell',
  imports: [RouterLink, RouterLinkActive, Icon],
  template: `
    <div class="min-h-screen">
      <aside class="fixed inset-y-0 left-0 z-30 hidden w-64 flex-col border-r border-slate-100 bg-white md:flex">
        <div class="flex h-16 items-center px-6">
          <span class="font-display text-2xl font-bold text-primary-500">{{ brand() }}</span>
        </div>
        <nav class="flex-1 space-y-1 px-3 py-4">
          @for (link of links(); track link.label) {
            @if (link.path) {
              <a
                [routerLink]="link.path"
                routerLinkActive="bg-primary-50 text-primary-700"
                #rla="routerLinkActive"
                class="flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-slate-600 transition hover:bg-slate-50">
                <span [class.text-primary-500]="rla.isActive"><app-icon [name]="link.icon" /></span>
                {{ link.label }}
              </a>
            } @else {
              <span
                class="flex cursor-not-allowed items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-slate-300"
                title="Em breve">
                <app-icon [name]="link.icon" />
                {{ link.label }}
              </span>
            }
          }
        </nav>
        <div class="border-t border-slate-100 p-3">
          @if (userName()) {
            <div class="flex items-center gap-3 px-3 py-2">
              <span class="flex h-9 w-9 items-center justify-center rounded-full bg-primary-100 font-display text-sm font-semibold text-primary-600">
                {{ initials() }}
              </span>
              <div class="min-w-0">
                <p class="truncate text-sm font-medium text-slate-700">{{ userName() }}</p>
                <p class="truncate text-xs text-slate-500">{{ userRole() }}</p>
              </div>
            </div>
          }
          <button
            type="button"
            class="mt-1 flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-accent-600 transition hover:bg-accent-50"
            (click)="logout.emit()">
            <app-icon name="logout" />
            Sair
          </button>
        </div>
      </aside>

      <div class="md:pl-64">
        <header class="sticky top-0 z-20 flex h-14 items-center justify-between border-b border-slate-100 bg-white/90 px-4 backdrop-blur md:hidden">
          <span class="font-display text-xl font-bold text-primary-500">{{ brand() }}</span>
          <div class="flex items-center gap-3">
            <button type="button" class="text-slate-500" aria-label="Notificações">
              <app-icon name="bell" />
            </button>
            @if (userName()) {
              <span class="flex h-8 w-8 items-center justify-center rounded-full bg-primary-100 font-display text-xs font-semibold text-primary-600">
                {{ initials() }}
              </span>
            }
          </div>
        </header>

        @if (title()) {
          <div class="px-4 pt-5 md:px-8 md:pt-8">
            <h1 class="font-display text-2xl font-bold text-slate-800">{{ title() }}</h1>
            @if (subtitle()) {
              <p class="mt-0.5 text-sm text-slate-500">{{ subtitle() }}</p>
            }
          </div>
        }

        <main class="px-4 pb-24 pt-4 md:px-8 md:pb-10">
          <ng-content />
        </main>
      </div>

      <nav class="fixed inset-x-0 bottom-0 z-30 flex border-t border-slate-100 bg-white md:hidden">
        @for (link of links(); track link.label) {
          @if (link.path) {
            <a
              [routerLink]="link.path"
              routerLinkActive="text-primary-600"
              class="flex flex-1 flex-col items-center gap-0.5 py-2 text-[11px] font-medium text-slate-500">
              <app-icon [name]="link.icon" [size]="22" />
              <span class="truncate px-1">{{ shortLabel(link.label) }}</span>
            </a>
          } @else {
            <span class="flex flex-1 flex-col items-center gap-0.5 py-2 text-[11px] font-medium text-slate-300">
              <app-icon [name]="link.icon" [size]="22" />
              <span class="truncate px-1">{{ shortLabel(link.label) }}</span>
            </span>
          }
        }
        <button
          type="button"
          class="flex flex-1 flex-col items-center gap-0.5 py-2 text-[11px] font-medium text-accent-500"
          (click)="logout.emit()">
          <app-icon name="logout" [size]="22" />
          <span>Sair</span>
        </button>
      </nav>
    </div>
  `
})
export class AppShell {
  readonly brand = input('Aure');
  readonly title = input('');
  readonly subtitle = input('');
  readonly userName = input('');
  readonly userRole = input('Profissional');
  readonly links = input<readonly ShellLink[]>(PROFESSIONAL_NAV);

  readonly logout = output<void>();

  protected initials(): string {
    return this.userName()
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((part) => part[0]?.toUpperCase() ?? '')
      .join('');
  }

  protected shortLabel(label: string): string {
    return label.split(' ')[0];
  }
}
