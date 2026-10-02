import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AppointmentResponse, ServiceResponse } from '../../../core/models/api.models';
import { AvailabilityService } from '../../../core/services/availability.service';
import { ProfessionalAppointmentService } from '../../../core/services/professional-appointment.service';
import { ToastService } from '../../../shared/feedback/toast.service';
import { Button } from '../../../shared/ui/button';
import { Spinner } from '../../../shared/ui/spinner';
import { TextField } from '../../../shared/ui/text-field';
import { apiErrorMessage, formatDuration, formatPrice } from '../../../shared/utils/format';
import { digitsOnly, maskPhoneBR } from '../../../shared/utils/masks';
import { isoToday } from './agenda-utils';

@Component({
  selector: 'app-new-appointment-form',
  imports: [FormsModule, Button, Spinner, TextField],
  template: `
    @if (services().length === 0) {
      <p class="text-sm text-slate-500">Cadastre um serviço ativo em Configurações para criar agendamentos.</p>
    } @else {
      <form class="space-y-4" (ngSubmit)="submit()">
        <app-text-field label="Serviço" [required]="true">
          <select class="field-input" name="service" [ngModel]="serviceId()" (ngModelChange)="serviceId.set(+$event)">
            @for (service of services(); track service.id) {
              <option [value]="service.id">
                {{ service.name }} · {{ formatDuration(service.duration_minutes) }} · {{ formatPrice(service.price) }}
              </option>
            }
          </select>
        </app-text-field>

        <app-text-field label="Data" [required]="true">
          <input class="field-input" type="date" name="date" [min]="today" [ngModel]="date()" (ngModelChange)="date.set($event)" />
        </app-text-field>

        <div>
          <span class="mb-1.5 block text-sm font-medium text-slate-700">Horário <span class="text-accent-500">*</span></span>
          @if (loadingSlots()) {
            <div class="flex justify-center py-3 text-primary-500"><app-spinner size="sm" /></div>
          } @else if (slots().length === 0) {
            <p class="rounded-xl bg-slate-50 px-3 py-2.5 text-sm text-slate-500">Nenhum horário livre neste dia.</p>
          } @else {
            <div class="grid grid-cols-4 gap-2 sm:grid-cols-5">
              @for (slot of slots(); track slot) {
                <button
                  type="button"
                  class="rounded-xl border py-2 text-sm font-medium transition"
                  [class]="slot === time()
                    ? 'border-primary-500 bg-primary-500 text-white'
                    : 'border-primary-100 bg-white text-slate-700 hover:border-primary-300'"
                  (click)="time.set(slot)">
                  {{ slot }}
                </button>
              }
            </div>
          }
        </div>

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

        <app-text-field label="Nome do cliente" hint="Usado se for o primeiro agendamento dele.">
          <input class="field-input" name="name" maxlength="120" [ngModel]="name()" (ngModelChange)="name.set($event)" />
        </app-text-field>

        <app-text-field label="Observações" hint="Só você vê.">
          <textarea
            class="field-input min-h-20"
            name="notes"
            maxlength="500"
            [ngModel]="notes()"
            (ngModelChange)="notes.set($event)"></textarea>
        </app-text-field>

        <app-button type="submit" [full]="true" [loading]="saving()" [disabled]="!canSubmit()">Agendar</app-button>
      </form>
    }
  `
})
export class NewAppointmentForm {
  private readonly availability = inject(AvailabilityService);
  private readonly appointments = inject(ProfessionalAppointmentService);
  private readonly toast = inject(ToastService);

  readonly professionalId = input.required<number>();
  readonly services = input.required<ServiceResponse[]>();
  readonly initialDate = input(isoToday());
  readonly initialTime = input<string | null>(null);
  readonly created = output<AppointmentResponse>();

  protected readonly today = isoToday();
  protected readonly serviceId = signal<number | null>(null);
  protected readonly date = signal(isoToday());
  protected readonly time = signal<string | null>(null);
  protected readonly slots = signal<string[]>([]);
  protected readonly loadingSlots = signal(false);
  protected readonly phone = signal('');
  protected readonly name = signal('');
  protected readonly notes = signal('');
  protected readonly saving = signal(false);

  protected readonly formatDuration = formatDuration;
  protected readonly formatPrice = formatPrice;
  protected readonly maskPhone = maskPhoneBR;

  protected readonly canSubmit = computed(
    () => !!this.serviceId() && !!this.date() && !!this.time() && digitsOnly(this.phone()).length >= 10
  );

  constructor() {
    effect(() => {
      this.date.set(this.initialDate() < this.today ? this.today : this.initialDate());
      this.serviceId.set(this.services()[0]?.id ?? null);
    });

    effect((onCleanup) => {
      const serviceId = this.serviceId();
      const date = this.date();
      this.time.set(null);
      this.slots.set([]);
      if (!serviceId || !date) return;

      this.loadingSlots.set(true);
      const sub = this.availability.getAvailableSlots(this.professionalId(), date, serviceId).subscribe({
        next: (res) => {
          this.slots.set(res.slots);
          const preferred = this.initialTime();
          if (preferred && date === this.initialDate() && res.slots.includes(preferred)) this.time.set(preferred);
          this.loadingSlots.set(false);
        },
        error: () => this.loadingSlots.set(false)
      });
      onCleanup(() => sub.unsubscribe());
    });
  }

  protected submit(): void {
    if (!this.canSubmit() || this.saving()) return;

    this.saving.set(true);
    this.appointments
      .create(this.professionalId(), {
        service_id: this.serviceId()!,
        scheduled_date: this.date(),
        scheduled_time: this.time()!,
        client_phone: digitsOnly(this.phone()),
        client_name: this.name().trim() || undefined,
        notes: this.notes().trim() || undefined
      })
      .subscribe({
        next: (appointment) => {
          this.saving.set(false);
          this.toast.success('Agendamento criado.');
          this.created.emit(appointment);
        },
        error: (err: HttpErrorResponse) => {
          this.saving.set(false);
          if (err.status > 0 && err.status < 500) {
            this.toast.error(apiErrorMessage(err, 'Não foi possível criar o agendamento.'));
          }
        }
      });
  }
}
