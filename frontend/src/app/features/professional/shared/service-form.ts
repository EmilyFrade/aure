import { Component, computed, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ServiceRequest, ServiceResponse } from '../../../core/models/api.models';
import { Button } from '../../../shared/ui/button';
import { TextField } from '../../../shared/ui/text-field';
import { formatDuration } from '../../../shared/utils/format';
import { maskCurrencyBR, parseCurrencyBR } from '../../../shared/utils/masks';

const DURATIONS = [15, 30, 45, 60, 90, 120];

@Component({
  selector: 'app-service-form',
  imports: [FormsModule, Button, TextField],
  template: `
    <form class="space-y-5" (ngSubmit)="submit()">
      <app-text-field label="Nome do serviço" [required]="true">
        <input
          class="field-input"
          name="serviceName"
          [ngModel]="name()"
          (ngModelChange)="name.set($event)"
          placeholder="Ex.: Corte feminino"
          maxlength="255" />
      </app-text-field>

      <fieldset>
        <legend class="mb-1.5 block text-sm font-medium text-slate-700">
          Duração <span class="text-accent-500">*</span>
        </legend>
        <div class="grid grid-cols-4 gap-2">
          @for (d of durations; track d) {
            <button
              type="button"
              (click)="selectDuration(d)"
              [attr.aria-pressed]="!customDuration() && duration() === d"
              class="rounded-xl border py-2.5 text-sm font-medium transition"
              [class]="chipClass(!customDuration() && duration() === d)">
              {{ formatDuration(d) }}
            </button>
          }
          <button
            type="button"
            (click)="customDuration.set(true)"
            [attr.aria-pressed]="customDuration()"
            class="col-span-2 rounded-xl border py-2.5 text-sm font-medium transition"
            [class]="chipClass(customDuration())">
            Outra
          </button>
        </div>
        @if (customDuration()) {
          <div class="mt-2 flex items-center gap-2">
            <input
              class="field-input w-28"
              type="number"
              name="customDuration"
              min="5"
              step="5"
              inputmode="numeric"
              [ngModel]="duration()"
              (ngModelChange)="duration.set(+$event)" />
            <span class="text-sm text-slate-500">minutos</span>
          </div>
        }
      </fieldset>

      <app-text-field label="Preço" [required]="true">
        <div class="relative">
          <span class="pointer-events-none absolute inset-y-0 left-4 flex items-center text-slate-400">R$</span>
          <input
            class="field-input pl-11"
            name="price"
            [ngModel]="price()"
            (ngModelChange)="price.set(maskCurrency($event))"
            inputmode="numeric"
            placeholder="0,00" />
        </div>
      </app-text-field>

      @if (withDescription()) {
        <app-text-field label="Descrição" hint="Opcional — aparece para o cliente ao agendar.">
          <textarea
            class="field-input min-h-20"
            name="description"
            maxlength="500"
            [ngModel]="description()"
            (ngModelChange)="description.set($event)"
            placeholder="Ex.: Inclui lavagem e finalização"></textarea>
        </app-text-field>
      }

      <div class="flex gap-2">
        @if (cancelable()) {
          <app-button variant="outline" [full]="true" (click)="cancel.emit()">Cancelar</app-button>
        }
        <app-button type="submit" [full]="true" [loading]="saving()" [disabled]="!valid()">
          {{ submitLabel() }}
        </app-button>
      </div>
    </form>
  `
})
export class ServiceForm {
  readonly initial = input<ServiceResponse | null>(null);
  readonly submitLabel = input('Salvar');
  readonly saving = input(false);
  readonly cancelable = input(false);
  readonly withDescription = input(false);

  readonly save = output<ServiceRequest>();
  readonly cancel = output<void>();

  protected readonly name = signal('');
  protected readonly duration = signal(60);
  protected readonly customDuration = signal(false);
  protected readonly price = signal('');
  protected readonly description = signal('');

  protected readonly durations = DURATIONS;
  protected readonly formatDuration = formatDuration;
  protected readonly maskCurrency = maskCurrencyBR;

  protected readonly valid = computed(
    () => !!this.name().trim() && !!this.price() && Number.isInteger(this.duration()) && this.duration() >= 5
  );

  ngOnInit(): void {
    const service = this.initial();
    if (!service) return;
    this.name.set(service.name);
    this.duration.set(service.duration_minutes);
    this.customDuration.set(!DURATIONS.includes(service.duration_minutes));
    this.price.set(maskCurrencyBR(service.price.toFixed(2)));
    this.description.set(service.description ?? '');
  }

  protected selectDuration(minutes: number): void {
    this.customDuration.set(false);
    this.duration.set(minutes);
  }

  protected chipClass(selected: boolean): string {
    return selected
      ? 'border-primary-500 bg-primary-50 text-primary-700'
      : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50';
  }

  protected submit(): void {
    if (!this.valid() || this.saving()) return;
    this.save.emit({
      name: this.name().trim(),
      duration_minutes: this.duration(),
      price: parseCurrencyBR(this.price()),
      ...(this.withDescription() ? { description: this.description().trim() || undefined } : {})
    });
  }
}
