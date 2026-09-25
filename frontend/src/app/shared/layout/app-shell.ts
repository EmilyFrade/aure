import { Component, HostListener, inject, input, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { ProfessionalAuthService } from '../../core/services/professional-auth.service';
import { ProfessionalService } from '../../core/services/professional.service';
import { Avatar } from '../ui/avatar';
import { Icon, IconName } from '../ui/icon';

interface ShellLink {
  readonly label: string;
  readonly icon: IconName;
  readonly path: string;
}

const PROFESSIONAL_NAV: readonly ShellLink[] = [
  { label: 'Agenda', icon: 'calendar-week', path: '/profissional/agenda' },
  { label: 'Configurações', icon: 'settings', path: '/profissional/configuracoes' }
];

@Component({
  selector: 'app-shell',
  imports: [RouterLink, RouterLinkActive, Avatar, Icon],
  template: `
    <div class="min-h-screen">
      <aside class="fixed inset-y-0 left-0 z-30 hidden w-64 flex-col border-r border-primary-100 bg-white md:flex">
        <div class="flex h-16 items-center px-6">
          <a routerLink="/profissional/agenda" class="font-display text-2xl font-bold text-primary-500">Aure</a>
        </div>
        <nav class="flex-1 space-y-1 px-3 py-4">
          @for (link of links; track link.path) {
            <a
              [routerLink]="link.path"
              routerLinkActive="bg-primary-50 text-primary-700"
              #rla="routerLinkActive"
              class="flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-slate-600 transition hover:bg-primary-50/60">
              <span [class]="rla.isActive ? 'text-primary-500' : 'text-slate-400'"><app-icon [name]="link.icon" /></span>
              {{ link.label }}
            </a>
          }
        </nav>
        <div class="border-t border-primary-100 p-3">
          <div class="flex items-center gap-3 px-3 py-2">
            <app-avatar [name]="userName()" [photoUrl]="photoUrl()" [size]="36" />
            <div class="min-w-0">
              <p class="truncate text-sm font-medium text-slate-700">{{ userName() || '…' }}</p>
              <p class="text-xs text-slate-500">Profissional</p>
            </div>
          </div>
          <button
            type="button"
            class="mt-1 flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-accent-600 transition hover:bg-accent-50"
            (click)="logout()">
            <app-icon name="logout" />
            Sair
          </button>
        </div>
      </aside>

      <div class="md:pl-64">
        <header class="sticky top-0 z-20 flex h-14 items-center justify-between border-b border-primary-100 bg-white/90 px-4 backdrop-blur md:hidden">
          <span class="font-display text-lg font-semibold text-slate-900">{{ title() }}</span>
          <div class="relative">
            <button type="button" class="flex rounded-full" aria-label="Menu da conta" (click)="toggleMenu($event)">
              <app-avatar [name]="userName()" [photoUrl]="photoUrl()" [size]="34" />
            </button>
            @if (menuOpen()) {
              <div class="absolute right-0 top-full z-40 mt-2 w-52 rounded-xl bg-white py-1 shadow-float ring-1 ring-primary-100">
                <p class="truncate border-b border-primary-100 px-4 py-2 text-sm font-medium text-slate-700">{{ userName() }}</p>
                <button
                  type="button"
                  class="flex w-full items-center gap-2 px-4 py-2 text-sm text-accent-600 hover:bg-accent-50"
                  (click)="logout()">
                  <app-icon name="logout" [size]="16" />
                  Sair
                </button>
              </div>
            }
          </div>
        </header>

        <div class="px-4 pt-4 md:px-8 md:pt-8">
          <div class="flex flex-wrap items-end justify-between gap-3">
            <div class="min-w-0">
              <h1 class="hidden font-display text-2xl font-bold text-slate-900 md:block">{{ title() }}</h1>
              @if (subtitle()) {
                <p class="text-sm text-slate-500 md:mt-0.5">{{ subtitle() }}</p>
              }
            </div>
            <ng-content select="[shellActions]" />
          </div>
        </div>

        <main class="px-4 pb-24 pt-5 md:px-8 md:pb-10">
          <ng-content />
        </main>
      </div>

      <nav class="fixed inset-x-0 bottom-0 z-30 flex border-t border-primary-100 bg-white pb-[env(safe-area-inset-bottom)] md:hidden">
        @for (link of links; track link.path) {
          <a
            [routerLink]="link.path"
            routerLinkActive="text-primary-600"
            #rla="routerLinkActive"
            class="flex flex-1 flex-col items-center gap-0.5 py-2 text-[11px] font-medium text-slate-500">
            <span class="flex h-7 w-12 items-center justify-center rounded-full transition" [class.bg-primary-100]="rla.isActive">
              <app-icon [name]="link.icon" [size]="20" />
            </span>
            {{ link.label }}
          </a>
        }
      </nav>
    </div>
  `
})
export class AppShell {
  private readonly auth = inject(ProfessionalAuthService);
  private readonly professionals = inject(ProfessionalService);
  private readonly router = inject(Router);

  readonly title = input('');
  readonly subtitle = input('');

  protected readonly links = PROFESSIONAL_NAV;
  protected readonly userName = signal('');
  protected readonly photoUrl = signal<string | null>(null);
  protected readonly menuOpen = signal(false);

  constructor() {
    const id = this.auth.professionalId;
    if (id) {
      this.professionals.get(id).subscribe((p) => {
        this.userName.set(p.name);
        this.photoUrl.set(p.photo_url);
      });
    }
  }

  @HostListener('document:click')
  protected closeMenu(): void {
    this.menuOpen.set(false);
  }

  protected toggleMenu(event: MouseEvent): void {
    event.stopPropagation();
    this.menuOpen.update((open) => !open);
  }

  protected logout(): void {
    this.auth.logout().subscribe({
      complete: () => this.router.navigate(['/profissional/login']),
      error: () => this.router.navigate(['/profissional/login'])
    });
  }
}
