import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BrandResponse } from '../../../core/models/api.models';
import { BrandService } from '../../../core/services/brand.service';
import { ProfessionalAuthService } from '../../../core/services/professional-auth.service';
import { AppShell } from '../../../shared/layout/app-shell';
import { Icon, IconName } from '../../../shared/ui/icon';
import { PublicLinkCard } from '../shared/public-link-card';
import { BlocksSettings } from './blocks-settings';
import { SchedulesSettings } from './schedules-settings';
import { ServicesSettings } from './services-settings';

type Tab = 'servicos' | 'horarios' | 'bloqueios';

const TABS: readonly { id: Tab; label: string; icon: IconName }[] = [
  { id: 'servicos', label: 'Serviços', icon: 'tag' },
  { id: 'horarios', label: 'Horários', icon: 'clock' },
  { id: 'bloqueios', label: 'Bloqueios', icon: 'lock' }
];

@Component({
  selector: 'app-professional-settings',
  imports: [AppShell, Icon, PublicLinkCard, ServicesSettings, SchedulesSettings, BlocksSettings],
  template: `
    <app-shell title="Configurações" subtitle="Serviços, horários e folgas da sua agenda.">
      <div class="mx-auto max-w-4xl">
        @if (brand(); as b) {
          <div class="mb-5">
            <app-public-link-card [slug]="b.slug" [brandName]="b.name" />
          </div>
        }

        <div class="mb-5 grid grid-cols-3 gap-1 rounded-2xl bg-primary-100/60 p-1" role="tablist">
          @for (t of tabs; track t.id) {
            <button
              type="button"
              role="tab"
              [attr.aria-selected]="tab() === t.id"
              (click)="selectTab(t.id)"
              class="flex items-center justify-center gap-1.5 rounded-xl py-2 text-sm font-medium transition"
              [class]="tab() === t.id ? 'bg-white text-primary-700 shadow-card' : 'text-slate-500 hover:text-primary-700'">
              <app-icon [name]="t.icon" [size]="16" />
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
  private readonly brands = inject(BrandService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly professionalId = this.auth.professionalId!;
  protected readonly tabs = TABS;
  protected readonly tab = signal<Tab>(this.initialTab());
  protected readonly brand = signal<BrandResponse | null>(null);

  constructor() {
    this.brands.forProfessional(this.professionalId).subscribe((b) => this.brand.set(b));
  }

  protected selectTab(tab: Tab): void {
    this.tab.set(tab);
    this.router.navigate([], { queryParams: { aba: tab }, replaceUrl: true });
  }

  private initialTab(): Tab {
    const aba = this.route.snapshot.queryParamMap.get('aba');
    return TABS.some((t) => t.id === aba) ? (aba as Tab) : 'servicos';
  }
}
