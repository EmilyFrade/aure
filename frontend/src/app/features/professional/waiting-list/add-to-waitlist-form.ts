import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DayPeriod, ServiceResponse, WaitingListEntry } from '../../../core/models/api.models';
import { WaitingListService } from '../../../core/services/waiting-list.service';
import { ToastService } from '../../../shared/feedback/toast.service';
import { Button } from '../../../shared/ui/button';
import { TextField } from '../../../shared/ui/text-field';
import { DAY_PERIODS, apiErrorMessage, formatDuration, formatWaitPeriod } from '../../../shared/utils/format';
import { digitsOnly, maskPhoneBR } from '../../../shared/utils/masks';
import { addDays, isoToday, parseIso } from '../agenda/agenda-utils';

const WAITLIST_DAYS = [1, 7, 15];

@Component({
  selector: 'app-add-to-waitlist-form',
  imports: [FormsModule, Button, TextField],
  template: `
    @if (services().length === 0) {
      <p class="text-sm text-slate-500">Cadastre um serviço ativo em Configurações para usar a fila.</p>
    } @else {
      <form class="space-y-4" (ngSubmit)="submit()">
        <app-text-field label="WhatsApp do cliente" [required]="true">
          <input
            class="field-input"
            type="tel"
            name="phone"
            inputmode="tel"
            autocomplete="off"
            placeholder="(31) 99999-9999"
            [ngModel]="phone()"
            (ngModelChange)="phone.set(maskPhone($event))" />
        </app-text-field>

        <app-text-field label="Nome do cliente" hint="Usado se for o primeiro contato dele.">
          <input class="field-input" name="name" maxlength="120" [ngModel]="name()" (ngModelChange)="name.set($event)" />
        </app-text-field>

        <app-text-field label="Serviço" [required]="true">
          <select class="field-input" name="service" [ngModel]="serviceId()" (ngModelChange)="serviceId.set(+$event)">
            @for (service of services(); track service.id) {
              <option [value]="service.id">{{ service.name }} · {{ formatDuration(service.duration_minutes) }}</option>
            }
          </select>
        </app-text-field>

        <app-text-field label="A partir de" [required]="true">
          <input class="field-input" type="date" name="start" [min]="today" [ngModel]="start()" (ngModelChange)="start.set($event)" />
        </app-text-field>

        <div>
          <span class="mb-1.5 block text-sm font-medium text-slate-700">Até quando pode esperar?</span>
          <div class="grid grid-cols-3 gap-2">
            @for (days of options; track days) {
              <button
                type="button"
                class="rounded-xl px-2 py-2 text-sm font-medium transition"
                [class]="days === waitDays() ? 'bg-primary-500 text-white shadow-md shadow-primary-500/30' : 'bg-primary-50 text-primary-700 hover:bg-primary-100'"
                (click)="waitDays.set(days)">
                {{ label(days) }}
              </button>
            }
          </div>
          @if (start()) {
            <p class="mt-2 text-xs text-slate-500">{{ formatWaitPeriod(start(), end(), period()) }}</p>
          }
        </div>

        <div>
          <p class="mb-1.5 block text-sm font-medium text-slate-700">Qual turno é melhor?</p>
          <div class="grid grid-cols-4 gap-2">
            @for (option of dayPeriods; track option.label) {
              <button
                type="button"
                (click)="period.set(option.value)"
                [attr.aria-pressed]="period() === option.value"
                class="rounded-xl px-1 py-2 text-sm font-medium transition"
                [class]="period() === option.value ? 'bg-primary-500 text-white shadow-md shadow-primary-500/30' : 'bg-primary-50 text-primary-700 hover:bg-primary-100'">
                {{ option.label }}
              </button>
            }
          </div>
        </div>

        <app-button type="submit" [full]="true" [loading]="saving()" [disabled]="!canSubmit()">Adicionar à fila</app-button>
      </form>
    }
  `
})
export class AddToWaitlistForm {
  private readonly waitingListService = inject(WaitingListService);
  private readonly toast = inject(ToastService);

  readonly professionalId = input.required<number>();
  readonly services = input.required<ServiceResponse[]>();
  readonly added = output<WaitingListEntry>();

  protected readonly today = isoToday();
  protected readonly options = WAITLIST_DAYS;
  protected readonly phone = signal('');
  protected readonly name = signal('');
  protected readonly serviceId = signal<number | null>(null);
  protected readonly start = signal(isoToday());
  protected readonly waitDays = signal(7);
  protected readonly period = signal<DayPeriod | null>(null);
  protected readonly dayPeriods = DAY_PERIODS;
  protected readonly saving = signal(false);

  protected readonly formatDuration = formatDuration;
  protected readonly formatWaitPeriod = formatWaitPeriod;
  protected readonly maskPhone = maskPhoneBR;

  protected readonly end = computed(() => addDays(this.start(), this.waitDays() - 1));

  protected readonly canSubmit = computed(
    () => !!this.serviceId() && !!this.start() && this.start() >= this.today && digitsOnly(this.phone()).length >= 10
  );

  constructor() {
    effect(() => this.serviceId.set(this.services()[0]?.id ?? null));
  }

  protected label(days: number): string {
    return days === 1 ? `Só dia ${parseIso(this.start() || this.today).getDate()}` : `Até ${days} dias`;
  }

  protected submit(): void {
    if (!this.canSubmit() || this.saving()) return;

    this.saving.set(true);
    this.waitingListService
      .add(this.professionalId(), {
        service_id: this.serviceId()!,
        client_phone: digitsOnly(this.phone()),
        client_name: this.name().trim() || undefined,
        preferred_date_start: this.start(),
        preferred_date_end: this.end(),
        preferred_period: this.period()
      })
      .subscribe({
        next: (entry) => {
          this.saving.set(false);
          this.toast.success('Cliente adicionado à fila.');
          this.added.emit(entry);
        },
        error: (err: HttpErrorResponse) => {
          this.saving.set(false);
          if (err.status > 0 && err.status < 500) {
            this.toast.error(apiErrorMessage(err, 'Não foi possível adicionar à fila.'));
          }
        }
      });
  }
}
