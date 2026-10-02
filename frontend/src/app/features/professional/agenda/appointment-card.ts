import { Component, HostListener, computed, input, output, signal } from '@angular/core';
import { AppointmentResponse, AppointmentStatus } from '../../../core/models/api.models';
import { Badge } from '../../../shared/ui/badge';
import { Button } from '../../../shared/ui/button';
import { Icon } from '../../../shared/ui/icon';
import { formatDuration, formatPrice } from '../../../shared/utils/format';
import { STATUS_BADGE, STATUS_BORDER, STATUS_LABEL, clientLabel, endTime, hasStarted } from './agenda-utils';

@Component({
  selector: 'app-appointment-card',
  imports: [Badge, Button, Icon],
  template: `
    @let a = appointment();
    <article
      class="rounded-2xl border-l-4 bg-white px-4 py-3 shadow-card ring-1 ring-primary-100/70"
      [class]="border()"
      [class.opacity-60]="a.status === 'CANCELLED' || a.status === 'NO_SHOW'">
      <div class="flex items-center gap-2">
        <span class="font-semibold tabular-nums text-slate-900">{{ a.scheduled_time }} – {{ end() }}</span>
        <app-badge class="ml-auto" [variant]="badge()">{{ label() }}</app-badge>
        @if (actionable()) {
          <div class="relative -mr-1.5">
            <button
              type="button"
              class="rounded-full p-1.5 text-slate-400 transition hover:bg-slate-100 hover:text-slate-600"
              aria-label="Mais ações"
              (click)="toggleMenu($event)">
              <app-icon name="more" [size]="18" />
            </button>
            @if (menuOpen()) {
              <div class="absolute right-0 top-full z-20 mt-1 w-48 rounded-xl bg-white py-1 shadow-float ring-1 ring-primary-100">
                @if (a.status === 'CONFIRMED') {
                  <button
                    type="button"
                    class="flex w-full items-center gap-2 px-4 py-2 text-left text-sm text-slate-700 hover:bg-primary-50"
                    (click)="statusChange.emit('NO_SHOW')">
                    <app-icon name="user-x" [size]="16" />
                    Cliente não veio
                  </button>
                }
                <button
                  type="button"
                  class="flex w-full items-center gap-2 px-4 py-2 text-left text-sm text-accent-600 hover:bg-accent-50"
                  (click)="cancel.emit()">
                  <app-icon name="x" [size]="16" />
                  Cancelar agendamento
                </button>
              </div>
            }
          </div>
        }
      </div>

      <p class="mt-1 truncate font-display text-base font-semibold text-slate-900">{{ name() }}</p>
      <p class="truncate text-sm text-slate-500">{{ a.service_name }} · {{ duration() }} · {{ price() }}</p>

      @if (a.notes) {
        <p class="mt-2 rounded-xl bg-primary-50/60 px-3 py-2 text-sm text-slate-600">{{ a.notes }}</p>
      }

      <div class="mt-3 flex items-center gap-2">
        <a
          [href]="'https://wa.me/55' + a.client_phone"
          target="_blank"
          rel="noopener"
          class="inline-flex h-9 items-center gap-1.5 rounded-xl bg-primary-50 px-3 text-sm font-medium text-primary-700 transition hover:bg-primary-100">
          <app-icon name="message" [size]="16" />
          WhatsApp
        </a>
        @if (a.status === 'PENDING') {
          <div class="ml-auto">
            <app-button size="sm" [loading]="busy()" (click)="statusChange.emit('CONFIRMED')">
              <app-icon name="check" [size]="16" />
              Confirmar
            </app-button>
          </div>
        } @else if (a.status === 'CONFIRMED' && started()) {
          <div class="ml-auto">
            <app-button size="sm" [loading]="busy()" (click)="statusChange.emit('COMPLETED')">
              <app-icon name="check" [size]="16" />
              Concluir
            </app-button>
          </div>
        }
      </div>
    </article>
  `
})
export class AppointmentCard {
  readonly appointment = input.required<AppointmentResponse>();
  readonly busy = input(false);
  readonly statusChange = output<AppointmentStatus>();
  readonly cancel = output<void>();

  protected readonly menuOpen = signal(false);

  protected readonly name = computed(() => clientLabel(this.appointment()));
  protected readonly label = computed(() => STATUS_LABEL[this.appointment().status]);
  protected readonly badge = computed(() => STATUS_BADGE[this.appointment().status]);
  protected readonly border = computed(() => STATUS_BORDER[this.appointment().status]);
  protected readonly end = computed(() => endTime(this.appointment()));
  protected readonly duration = computed(() => formatDuration(this.appointment().duration_minutes));
  protected readonly price = computed(() => formatPrice(this.appointment().price));
  protected readonly started = computed(() => hasStarted(this.appointment()));
  protected readonly actionable = computed(() => ['PENDING', 'CONFIRMED'].includes(this.appointment().status));

  @HostListener('document:click')
  protected closeMenu(): void {
    this.menuOpen.set(false);
  }

  protected toggleMenu(event: MouseEvent): void {
    event.stopPropagation();
    this.menuOpen.update((open) => !open);
  }
}
