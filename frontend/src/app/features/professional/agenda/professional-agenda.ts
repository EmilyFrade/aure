import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AppointmentResponse, AppointmentStatus } from '../../../core/models/api.models';
import { ProfessionalAppointmentService } from '../../../core/services/professional-appointment.service';
import { ProfessionalAuthService } from '../../../core/services/professional-auth.service';

type RangeMode = 'day' | 'week';

@Component({
  selector: 'app-professional-agenda',
  imports: [FormsModule],
  templateUrl: './professional-agenda.html'
})
export class ProfessionalAgenda {
  private readonly appointmentService = inject(ProfessionalAppointmentService);
  private readonly authService = inject(ProfessionalAuthService);
  private readonly router = inject(Router);

  readonly rangeMode = signal<RangeMode>('day');
  readonly referenceDate = signal(new Date().toISOString().slice(0, 10));
  readonly appointments = signal<AppointmentResponse[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  constructor() {
    this.load();
  }

  setRangeMode(mode: RangeMode): void {
    this.rangeMode.set(mode);
    this.load();
  }

  load(): void {
    const professionalId = this.authService.professionalId;
    if (!professionalId) return;

    const start = this.referenceDate();
    const end = this.rangeMode() === 'week' ? this.addDays(start, 6) : start;

    this.loading.set(true);
    this.errorMessage.set(null);
    this.appointmentService.list(professionalId, { startDate: start, endDate: end }).subscribe({
      next: (appointments) => {
        this.loading.set(false);
        this.appointments.set(
          [...appointments].sort((a, b) => `${a.scheduled_date}${a.scheduled_time}`.localeCompare(`${b.scheduled_date}${b.scheduled_time}`))
        );
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Não foi possível carregar a agenda.');
      }
    });
  }

  updateStatus(appointment: AppointmentResponse, status: AppointmentStatus): void {
    const professionalId = this.authService.professionalId;
    if (!professionalId) return;

    this.appointmentService.updateStatus(professionalId, appointment.id, status).subscribe({
      next: () => this.load(),
      error: () => this.errorMessage.set('Não foi possível atualizar o status.')
    });
  }

  cancel(appointment: AppointmentResponse): void {
    const professionalId = this.authService.professionalId;
    if (!professionalId) return;

    this.appointmentService.cancel(professionalId, appointment.id).subscribe({
      next: () => this.load(),
      error: () => this.errorMessage.set('Não foi possível cancelar o agendamento.')
    });
  }

  logout(): void {
    this.authService.logout().subscribe(() => this.router.navigate(['/profissional/login']));
  }

  private addDays(date: string, days: number): string {
    const result = new Date(`${date}T00:00:00`);
    result.setDate(result.getDate() + days);
    return result.toISOString().slice(0, 10);
  }
}
