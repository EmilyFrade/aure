import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { DayOfWeek, ServiceRequest } from '../../../core/models/api.models';
import { ProfessionalAuthService } from '../../../core/services/professional-auth.service';
import { ScheduleService } from '../../../core/services/schedule.service';
import { ServiceCatalogService } from '../../../core/services/service-catalog.service';
import { ToastService } from '../../../shared/feedback/toast.service';
import { PublicShell } from '../../../shared/layout/public-shell';
import { Button } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { Icon } from '../../../shared/ui/icon';
import { Spinner } from '../../../shared/ui/spinner';
import { TextField } from '../../../shared/ui/text-field';
import { WEEK_DAYS } from '../../../shared/utils/format';
import { ServiceForm } from '../shared/service-form';

type Step = 'loading' | 'service' | 'schedule';

@Component({
  selector: 'app-professional-setup',
  imports: [FormsModule, RouterLink, PublicShell, Card, Button, TextField, Icon, Spinner, ServiceForm],
  templateUrl: './professional-setup.html'
})
export class ProfessionalSetup {
  private readonly auth = inject(ProfessionalAuthService);
  private readonly services = inject(ServiceCatalogService);
  private readonly schedules = inject(ScheduleService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  private readonly professionalId = this.auth.professionalId!;

  readonly step = signal<Step>('loading');
  readonly saving = signal(false);

  readonly selectedDays = signal<ReadonlySet<DayOfWeek>>(
    new Set(['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'])
  );
  readonly startTime = signal('09:00');
  readonly endTime = signal('18:00');

  protected readonly days = WEEK_DAYS;

  readonly timeError = computed(() =>
    this.startTime() && this.endTime() && this.startTime() >= this.endTime()
      ? 'O término deve ser depois do início.'
      : null
  );

  readonly canSaveSchedule = computed(
    () => this.selectedDays().size > 0 && !!this.startTime() && !!this.endTime() && !this.timeError()
  );

  constructor() {
    forkJoin([this.services.list(this.professionalId), this.schedules.list(this.professionalId)]).subscribe({
      next: ([services, schedules]) => {
        if (services.length === 0) this.step.set('service');
        else if (schedules.length === 0) this.step.set('schedule');
        else this.router.navigate(['/profissional/agenda']);
      },
      error: () => this.step.set('service')
    });
  }

  toggleDay(day: DayOfWeek): void {
    this.selectedDays.update((current) => {
      const next = new Set(current);
      if (next.has(day)) next.delete(day);
      else next.add(day);
      return next;
    });
  }

  saveService(request: ServiceRequest): void {
    this.saving.set(true);
    this.services.create(this.professionalId, request).subscribe({
      next: () => {
        this.saving.set(false);
        this.step.set('schedule');
      },
      error: (err) => {
        this.saving.set(false);
        if (err.status === 400) this.toast.error('Confira os dados do serviço e tente novamente.');
      }
    });
  }

  saveSchedule(): void {
    if (!this.canSaveSchedule() || this.saving()) return;

    const requests = WEEK_DAYS.filter((d) => this.selectedDays().has(d.value)).map((d) =>
      this.schedules.create(this.professionalId, {
        day_of_week: d.value,
        start_time: this.startTime(),
        end_time: this.endTime()
      })
    );

    this.saving.set(true);
    forkJoin(requests).subscribe({
      next: () => {
        this.saving.set(false);
        this.toast.success('Tudo pronto! Sua agenda já está aberta para agendamentos.');
        this.router.navigate(['/profissional/agenda']);
      },
      error: (err) => {
        this.saving.set(false);
        if (err.status === 400) this.toast.error('Confira os horários e tente novamente.');
      }
    });
  }
}
