import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BrandResponse } from '../../../core/models/api.models';
import { BrandService } from '../../../core/services/brand.service';
import { ProfessionalAuthService } from '../../../core/services/professional-auth.service';
import { ProfessionalService } from '../../../core/services/professional.service';
import { AppShell } from '../../../shared/layout/app-shell';
import { PublicLinkCard } from '../shared/public-link-card';
import { BlocksSettings } from './blocks-settings';
import { SchedulesSettings } from './schedules-settings';
import { ServicesSettings } from './services-settings';

type Tab = 'servicos' | 'horarios' | 'bloqueios';

const TABS: readonly { id: Tab; label: string }[] = [
  { id: 'servicos', label: 'Serviços' },
  { id: 'horarios', label: 'Horários' },
  { id: 'bloqueios', label: 'Bloqueios' }
];

@Component({
  selector: 'app-professional-settings',
  imports: [AppShell, PublicLinkCard, ServicesSettings, SchedulesSettings, BlocksSettings],
  template: `
    <app-shell title="Configurações" [userName]="userName()" (logout)="logout()">
      <div class="mx-auto max-w-4xl">
        @if (brand(); as b) {
          <div class="mb-5">
            <app-public-link-card [slug]="b.slug" [brandName]="b.name" />
          </div>
        }

        <div class="mb-5 grid grid-cols-3 gap-1 rounded-xl bg-primary-50 p-1" role="tablist">
          @for (t of tabs; track t.id) {
            <button
              type="button"
              role="tab"
              [attr.aria-selected]="tab() === t.id"
              (click)="selectTab(t.id)"
              class="rounded-lg py-2 text-sm font-medium transition"
              [class]="tab() === t.id ? 'bg-white text-primary-700 shadow-sm' : 'text-slate-500 hover:text-primary-700'">
              {{ t.label }}
            </button>
          }
        </div>

        @switch (tab()) {
          @case ('servicos') {
            <app-services-settings [professionalId]="professionalId" />
          }
          @case ('horarios') {
            <app-schedules-settings [professionalId]="professionalId" />
          }
          @case ('bloqueios') {
            <app-blocks-settings [professionalId]="professionalId" />
          }
        }
      </div>
    </app-shell>
  `
})
export class ProfessionalSettings {
  private readonly auth = inject(ProfessionalAuthService);
  private readonly professionals = inject(ProfessionalService);
  private readonly brands = inject(BrandService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly professionalId = this.auth.professionalId!;
  protected readonly tabs = TABS;
  protected readonly tab = signal<Tab>(this.initialTab());
  protected readonly userName = signal('');
  protected readonly brand = signal<BrandResponse | null>(null);

  constructor() {
    this.professionals.get(this.professionalId).subscribe((p) => {
      this.userName.set(p.name);
      this.brands.get(p.brand_id).subscribe((b) => this.brand.set(b));
    });
  }

  protected selectTab(tab: Tab): void {
    this.tab.set(tab);
    this.router.navigate([], { queryParams: { aba: tab }, replaceUrl: true });
  }

  protected logout(): void {
    this.auth.logout().subscribe(() => this.router.navigate(['/profissional/login']));
  }

  private initialTab(): Tab {
    const aba = this.route.snapshot.queryParamMap.get('aba');
    return TABS.some((t) => t.id === aba) ? (aba as Tab) : 'servicos';
  }
}
