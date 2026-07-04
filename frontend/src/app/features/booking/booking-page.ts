import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { AvailabilityService } from '../../core/services/availability.service';
import { ClientAuthService } from '../../core/services/client-auth.service';
import { AppointmentService } from '../../core/services/appointment.service';
import { PublicProfileService } from '../../core/services/public-profile.service';
import {
  AppointmentResponse,
  PublicProfessional,
  PublicProfile,
  PublicService
} from '../../core/models/api.models';

type Step = 'loading' | 'not-found' | 'choose' | 'schedule' | 'identify' | 'verify' | 'confirm' | 'done';

@Component({
  selector: 'app-booking-page',
  imports: [FormsModule],
  templateUrl: './booking-page.html'
})
export class BookingPage {
  private readonly route = inject(ActivatedRoute);
  private readonly publicProfileService = inject(PublicProfileService);
  private readonly availabilityService = inject(AvailabilityService);
  private readonly clientAuthService = inject(ClientAuthService);
  private readonly appointmentService = inject(AppointmentService);

  readonly step = signal<Step>('loading');
  readonly errorMessage = signal<string | null>(null);
  readonly submitting = signal(false);

  readonly profile = signal<PublicProfile | null>(null);
  readonly selectedProfessional = signal<PublicProfessional | null>(null);
  readonly selectedService = signal<PublicService | null>(null);

  readonly today = new Date().toISOString().slice(0, 10);
  readonly selectedDate = signal(this.today);
  readonly slots = signal<string[]>([]);
  readonly selectedSlot = signal<string | null>(null);

  readonly phone = signal('');
  readonly otpCode = signal('');
  readonly notes = signal('');

  readonly confirmedAppointment = signal<AppointmentResponse | null>(null);

  constructor() {
    const slug = this.route.snapshot.paramMap.get('slug');
    if (!slug) {
      this.step.set('not-found');
      return;
    }

    this.publicProfileService.getBySlug(slug).subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.step.set('choose');
      },
      error: () => this.step.set('not-found')
    });
  }

  chooseService(professional: PublicProfessional, service: PublicService): void {
    this.selectedProfessional.set(professional);
    this.selectedService.set(service);
    this.step.set('schedule');
    this.loadAvailability();
  }

  loadAvailability(): void {
    const professional = this.selectedProfessional();
    const service = this.selectedService();
    if (!professional || !service) return;

    this.errorMessage.set(null);
    this.selectedSlot.set(null);
    this.availabilityService.getAvailableSlots(professional.id, this.selectedDate(), service.id).subscribe({
      next: (response) => this.slots.set(response.slots),
      error: () => this.errorMessage.set('Não foi possível carregar os horários disponíveis.')
    });
  }

  pickSlot(slot: string): void {
    this.selectedSlot.set(slot);
    this.step.set('identify');
  }

  requestCode(): void {
    if (!this.phone().trim()) return;

    this.errorMessage.set(null);
    this.submitting.set(true);
    this.clientAuthService.requestOtp(this.phone().trim()).subscribe({
      next: () => {
        this.submitting.set(false);
        this.step.set('verify');
      },
      error: () => {
        this.submitting.set(false);
        this.errorMessage.set('Não foi possível enviar o código. Verifique o telefone informado.');
      }
    });
  }

  verifyCode(): void {
    if (!this.otpCode().trim()) return;

    this.errorMessage.set(null);
    this.submitting.set(true);
    this.clientAuthService.verifyOtp(this.phone().trim(), this.otpCode().trim()).subscribe({
      next: () => {
        this.submitting.set(false);
        this.step.set('confirm');
      },
      error: () => {
        this.submitting.set(false);
        this.errorMessage.set('Código inválido ou expirado.');
      }
    });
  }

  confirmAppointment(): void {
    const professional = this.selectedProfessional();
    const service = this.selectedService();
    const slot = this.selectedSlot();
    if (!professional || !service || !slot) return;

    this.errorMessage.set(null);
    this.submitting.set(true);
    this.appointmentService
      .create({
        professional_id: professional.id,
        service_id: service.id,
        scheduled_date: this.selectedDate(),
        scheduled_time: slot,
        notes: this.notes().trim() || undefined
      })
      .subscribe({
        next: (appointment) => {
          this.submitting.set(false);
          this.confirmedAppointment.set(appointment);
          this.step.set('done');
        },
        error: (err) => {
          this.submitting.set(false);
          if (err.status === 409) {
            this.errorMessage.set('Esse horário acabou de ser ocupado. Escolha outro.');
            this.step.set('schedule');
            this.loadAvailability();
          } else {
            this.errorMessage.set('Não foi possível confirmar o agendamento.');
          }
        }
      });
  }

  backToSchedule(): void {
    this.step.set('schedule');
    this.errorMessage.set(null);
  }

  backToChoose(): void {
    this.step.set('choose');
    this.selectedProfessional.set(null);
    this.selectedService.set(null);
    this.errorMessage.set(null);
  }
}
