import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Observable, switchMap } from 'rxjs';
import {
  AppointmentResponse,
  PublicProfile,
  DayPeriod,
  PublicService,
  WaitingListEntry
} from '../../core/models/api.models';
import { AppointmentService } from '../../core/services/appointment.service';
import { AvailabilityService } from '../../core/services/availability.service';
import { ClientAuthService } from '../../core/services/client-auth.service';
import { PublicProfileService } from '../../core/services/public-profile.service';
import { SessionStore } from '../../core/services/session-store';
import { WaitingListService } from '../../core/services/waiting-list.service';
import { ToastService } from '../../shared/feedback/toast.service';
import { PublicShell } from '../../shared/layout/public-shell';
import { ActionBar } from '../../shared/ui/action-bar';
import { Avatar } from '../../shared/ui/avatar';
import { Button } from '../../shared/ui/button';
import { Calendar } from '../../shared/ui/calendar';
import { Card } from '../../shared/ui/card';
import { EmptyState } from '../../shared/ui/empty-state';
import { Icon, IconName } from '../../shared/ui/icon';
import { OtpInput } from '../../shared/ui/otp-input';
import { Spinner } from '../../shared/ui/spinner';
import { Stepper } from '../../shared/ui/stepper';
import { TextField } from '../../shared/ui/text-field';
import { DAY_PERIODS, apiErrorMessage, formatDuration, formatPrice, formatWaitPeriod } from '../../shared/utils/format';
import { digitsOnly, maskPhoneBR } from '../../shared/utils/masks';

type Step = 'loading' | 'not-found' | 'profile' | 'schedule' | 'confirm' | 'done';
type VerifyPhase = 'checking' | 'phone' | 'code' | 'name' | 'ready';
type Mode = 'appointment' | 'waitlist';

const BOOKING_WINDOW_DAYS = 60;
const RESEND_SECONDS = 30;
const OTP_LENGTH = 6;
const WAITLIST_DAYS = [1, 7, 15];

const longDate = new Intl.DateTimeFormat('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' });
const shortDate = new Intl.DateTimeFormat('pt-BR', { day: 'numeric', month: 'long' });

interface SlotGroup {
  label: string;
  icon: IconName;
  slots: string[];
}

@Component({
  selector: 'app-booking-page',
  imports: [
    FormsModule,
    RouterLink,
    PublicShell,
    ActionBar,
    Avatar,
    Button,
    Calendar,
    Card,
    EmptyState,
    Icon,
    OtpInput,
    Spinner,
    Stepper,
    TextField
  ],
  templateUrl: './booking-page.html'
})
export class BookingPage {
  private readonly route = inject(ActivatedRoute);
  private readonly publicProfileService = inject(PublicProfileService);
  private readonly availabilityService = inject(AvailabilityService);
  private readonly clientAuthService = inject(ClientAuthService);
  private readonly appointmentService = inject(AppointmentService);
  private readonly waitingListService = inject(WaitingListService);
  private readonly session = inject(SessionStore);
  private readonly toast = inject(ToastService);

  protected readonly steps = ['Serviço', 'Data e hora', 'Confirmação'];
  protected readonly today = isoToday();
  protected readonly maxDate = addDays(this.today, BOOKING_WINDOW_DAYS);

  protected readonly step = signal<Step>('loading');
  protected readonly profile = signal<PublicProfile | null>(null);
  protected readonly selectedService = signal<PublicService | null>(null);

  protected readonly selectedDate = signal(this.today);
  protected readonly slots = signal<string[]>([]);
  protected readonly loadingSlots = signal(false);
  protected readonly selectedSlot = signal<string | null>(null);

  protected readonly verifyPhase = signal<VerifyPhase>('phone');
  protected readonly phone = signal('');
  protected readonly otp = signal('');
  protected readonly otpError = signal<string | null>(null);
  protected readonly sendingCode = signal(false);
  protected readonly verifying = signal(false);
  protected readonly clientName = signal<string | null>(null);
  protected readonly nameInput = signal('');
  protected readonly submitting = signal(false);
  protected readonly resendIn = signal(0);

  protected readonly confirmed = signal<AppointmentResponse | null>(null);

  protected readonly mode = signal<Mode>('appointment');
  protected readonly waitlistOptions = WAITLIST_DAYS;
  protected readonly waitlistDays = signal(7);
  protected readonly waitlistPeriod = signal<DayPeriod | null>(null);
  protected readonly dayPeriods = DAY_PERIODS;
  protected readonly joined = signal<WaitingListEntry | null>(null);

  protected readonly formatPrice = formatPrice;
  protected readonly formatDuration = formatDuration;
  protected readonly maskPhone = maskPhoneBR;
  protected readonly formatWaitPeriod = formatWaitPeriod;

  protected readonly mapsUrl = computed(() => {
    const brand = this.profile();
    if (!brand?.address) return null;
    const query = [brand.address, brand.city, brand.state].filter(Boolean).join(', ');
    return `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(query)}`;
  });

  protected readonly whatsAppUrl = computed(() => {
    const phone = digitsOnly(this.profile()?.professional.phone ?? '');
    return phone.length >= 10 ? `https://wa.me/55${phone}` : null;
  });

  protected readonly slotGroups = computed<SlotGroup[]>(() => {
    const groups: SlotGroup[] = [
      { label: 'Manhã', icon: 'sun', slots: [] },
      { label: 'Tarde', icon: 'sunset', slots: [] },
      { label: 'Noite', icon: 'moon', slots: [] }
    ];
    for (const slot of this.slots()) {
      const hour = Number(slot.slice(0, 2));
      groups[hour < 12 ? 0 : hour < 18 ? 1 : 2].slots.push(slot);
    }
    return groups.filter((g) => g.slots.length > 0);
  });

  protected readonly slotEnd = computed(() => {
    const slot = this.selectedSlot();
    const service = this.selectedService();
    return slot && service ? addMinutes(slot, service.duration_minutes) : null;
  });

  protected readonly waitlistEnd = computed(() => {
    const end = addDays(this.selectedDate(), this.waitlistDays() - 1);
    return end > this.maxDate ? this.maxDate : end;
  });

  protected waitlistLabel(days: number): string {
    return days === 1 ? `Só dia ${parseIso(this.selectedDate()).getDate()}` : `Até ${days} dias`;
  }

  protected readonly canRequestCode = computed(() => digitsOnly(this.phone()).length >= 10);

  protected readonly canConfirm = computed(
    () => this.verifyPhase() === 'ready' || (this.verifyPhase() === 'name' && this.nameInput().trim().length >= 2)
  );

  protected readonly firstName = computed(() => this.clientName()?.trim().split(' ')[0] ?? '');

  protected readonly containerClass = computed(() => {
    switch (this.step()) {
      case 'profile':
        return 'max-w-5xl';
      case 'schedule':
      case 'confirm':
        return 'max-w-4xl pb-32 sm:pb-0';
      default:
        return 'max-w-lg';
    }
  });

  private countdown: ReturnType<typeof setInterval> | null = null;

  constructor() {
    inject(DestroyRef).onDestroy(() => this.stopCountdown());

    const slug = this.route.snapshot.paramMap.get('slug');
    if (!slug) {
      this.step.set('not-found');
      return;
    }

    this.publicProfileService.getBySlug(slug).subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.step.set('profile');
      },
      error: () => this.step.set('not-found')
    });
  }

  protected chooseService(service: PublicService): void {
    this.selectedService.set(service);
    this.selectedSlot.set(null);
    this.goTo('schedule');
    this.loadAvailability();
  }

  protected pickDate(iso: string): void {
    this.selectedDate.set(iso);
    this.loadAvailability();
  }

  protected pickSlot(slot: string): void {
    this.selectedSlot.set(slot);
  }

  protected advance(): void {
    if (!this.selectedSlot()) return;
    this.mode.set('appointment');
    this.openConfirm();
  }

  protected joinWaitlist(): void {
    this.selectedSlot.set(null);
    this.mode.set('waitlist');
    this.openConfirm();
  }

  private openConfirm(): void {
    this.otp.set('');
    this.otpError.set(null);
    this.goTo('confirm');
    if (this.session.clientToken) this.checkSession();
    else this.verifyPhase.set('phone');
  }

  protected backToProfile(): void {
    this.selectedSlot.set(null);
    this.goTo('profile');
  }

  protected backToSchedule(): void {
    this.mode.set('appointment');
    this.joined.set(null);
    this.goTo('schedule');
    this.loadAvailability();
  }

  protected bookAnother(): void {
    this.confirmed.set(null);
    this.joined.set(null);
    this.mode.set('appointment');
    this.selectedService.set(null);
    this.selectedSlot.set(null);
    this.goTo('profile');
  }

  protected requestCode(): void {
    if (!this.canRequestCode() || this.sendingCode()) return;

    this.sendingCode.set(true);
    this.otpError.set(null);
    this.clientAuthService.requestOtp(digitsOnly(this.phone())).subscribe({
      next: () => {
        this.sendingCode.set(false);
        this.otp.set('');
        this.verifyPhase.set('code');
        this.startCountdown();
      },
      error: () => {
        this.sendingCode.set(false);
        this.toast.error('Não foi possível enviar o código. Confira o número.');
      }
    });
  }

  protected onOtpChange(value: string): void {
    this.otp.set(value);
    this.otpError.set(null);
    if (value.length === OTP_LENGTH) this.verifyCode();
  }

  protected verifyCode(): void {
    if (this.otp().length !== OTP_LENGTH || this.verifying()) return;

    this.verifying.set(true);
    this.clientAuthService.verifyOtp(digitsOnly(this.phone()), this.otp()).subscribe({
      next: (response) => {
        this.verifying.set(false);
        this.stopCountdown();
        this.clientName.set(response.name);
        this.verifyPhase.set(response.name ? 'ready' : 'name');
      },
      error: () => {
        this.verifying.set(false);
        this.otpError.set('Código inválido ou expirado.');
      }
    });
  }

  protected changePhone(): void {
    this.otp.set('');
    this.otpError.set(null);
    this.verifyPhase.set('phone');
  }

  protected useAnotherNumber(): void {
    this.session.clearClientSession();
    this.clientName.set(null);
    this.changePhone();
  }

  protected confirm(): void {
    const professional = this.profile()?.professional;
    const service = this.selectedService();
    if (!professional || !service || !this.canConfirm() || this.submitting()) return;

    if (this.mode() === 'waitlist') {
      this.submitWaitlist(professional.id, service);
      return;
    }

    const slot = this.selectedSlot();
    if (!slot) return;

    this.submitting.set(true);
    this.withName(() =>
      this.appointmentService.create({
        professional_id: professional.id,
        service_id: service.id,
        scheduled_date: this.selectedDate(),
        scheduled_time: slot
      })
    ).subscribe({
      next: (appointment) => {
        this.submitting.set(false);
        this.confirmed.set(appointment);
        this.goTo('done');
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.handleConfirmError(err);
      }
    });
  }

  private submitWaitlist(professionalId: number, service: PublicService): void {
    this.submitting.set(true);
    this.withName(() =>
      this.waitingListService.join({
        professional_id: professionalId,
        service_id: service.id,
        preferred_date_start: this.selectedDate(),
        preferred_date_end: this.waitlistEnd(),
        preferred_period: this.waitlistPeriod()
      })
    ).subscribe({
      next: (entry) => {
        this.submitting.set(false);
        this.joined.set(entry);
        this.goTo('done');
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        if (err.status === 401) this.handleConfirmError(err);
        else if (err.status > 0 && err.status < 500) this.toast.error(apiErrorMessage(err, 'Não foi possível entrar na fila.'));
      }
    });
  }

  private withName<T>(request: () => Observable<T>): Observable<T> {
    if (this.verifyPhase() !== 'name') return request();
    return this.clientAuthService.updateName(this.nameInput().trim()).pipe(
      switchMap((client) => {
        this.clientName.set(client.name);
        this.verifyPhase.set('ready');
        return request();
      })
    );
  }

  protected describeDate(iso: string): string {
    return capitalize(longDate.format(parseIso(iso)));
  }

  protected shortDate(iso: string): string {
    return shortDate.format(parseIso(iso));
  }

  private checkSession(): void {
    this.verifyPhase.set('checking');
    this.clientAuthService.me().subscribe({
      next: (client) => {
        this.clientName.set(client.name);
        this.phone.set(maskPhoneBR(client.phone));
        this.verifyPhase.set(client.name ? 'ready' : 'name');
      },
      error: () => {
        this.session.clearClientSession();
        this.verifyPhase.set('phone');
      }
    });
  }

  private handleConfirmError(err: HttpErrorResponse): void {
    if (err.status === 401) {
      this.session.clearClientSession();
      this.clientName.set(null);
      this.changePhone();
      this.toast.info('Confirme seu telefone para continuar.');
    } else if (err.status === 409) {
      this.toast.error(apiErrorMessage(err, 'Esse horário acabou de ser ocupado. Escolha outro.'));
      this.selectedSlot.set(null);
      this.backToSchedule();
    } else if (err.status > 0 && err.status < 500) {
      this.toast.error('Não foi possível confirmar o agendamento.');
    }
  }

  private loadAvailability(): void {
    const professional = this.profile()?.professional;
    const service = this.selectedService();
    if (!professional || !service) return;

    this.loadingSlots.set(true);
    this.availabilityService.getAvailableSlots(professional.id, this.selectedDate(), service.id).subscribe({
      next: (response) => {
        this.slots.set(response.slots);
        if (this.selectedSlot() && !response.slots.includes(this.selectedSlot()!)) this.selectedSlot.set(null);
        this.loadingSlots.set(false);
      },
      error: () => {
        this.slots.set([]);
        this.loadingSlots.set(false);
        this.toast.error('Não foi possível carregar os horários.');
      }
    });
  }

  private goTo(step: Step): void {
    this.step.set(step);
    window.scrollTo({ top: 0 });
  }

  private startCountdown(): void {
    this.stopCountdown();
    this.resendIn.set(RESEND_SECONDS);
    this.countdown = setInterval(() => {
      this.resendIn.update((s) => s - 1);
      if (this.resendIn() <= 0) this.stopCountdown();
    }, 1000);
  }

  private stopCountdown(): void {
    if (this.countdown) clearInterval(this.countdown);
    this.countdown = null;
  }
}

function parseIso(iso: string): Date {
  return new Date(`${iso}T12:00:00`);
}

function isoToday(): string {
  return new Date().toLocaleDateString('sv-SE');
}

function addDays(iso: string, days: number): string {
  const date = parseIso(iso);
  date.setDate(date.getDate() + days);
  return date.toLocaleDateString('sv-SE');
}

function addMinutes(time: string, minutes: number): string {
  const [h, m] = time.split(':').map(Number);
  const total = (h * 60 + m + minutes) % (24 * 60);
  return `${String(Math.floor(total / 60)).padStart(2, '0')}:${String(total % 60).padStart(2, '0')}`;
}

function capitalize(text: string): string {
  return text.charAt(0).toUpperCase() + text.slice(1);
}
