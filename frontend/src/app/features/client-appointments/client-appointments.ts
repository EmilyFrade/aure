import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AppointmentResponse, AppointmentStatus, WaitingListEntry } from '../../core/models/api.models';
import { AppointmentService } from '../../core/services/appointment.service';
import { ClientAuthService } from '../../core/services/client-auth.service';
import { SessionStore } from '../../core/services/session-store';
import { WaitingListService } from '../../core/services/waiting-list.service';
import { ToastService } from '../../shared/feedback/toast.service';
import { PublicShell } from '../../shared/layout/public-shell';
import { Avatar } from '../../shared/ui/avatar';
import { Badge, BadgeVariant } from '../../shared/ui/badge';
import { Button } from '../../shared/ui/button';
import { Card } from '../../shared/ui/card';
import { EmptyState } from '../../shared/ui/empty-state';
import { Icon } from '../../shared/ui/icon';
import { OtpInput } from '../../shared/ui/otp-input';
import { Sheet } from '../../shared/ui/sheet';
import { Spinner } from '../../shared/ui/spinner';
import { TextField } from '../../shared/ui/text-field';
import { apiErrorMessage, formatWaitPeriod, formatPrice } from '../../shared/utils/format';
import { digitsOnly, maskPhoneBR } from '../../shared/utils/masks';

type Phase = 'checking' | 'phone' | 'code' | 'list';
type Tab = 'upcoming' | 'history';

const RESEND_SECONDS = 30;
const OTP_LENGTH = 6;
const OPEN_STATUSES: readonly AppointmentStatus[] = ['PENDING', 'CONFIRMED'];

const STATUS: Record<AppointmentStatus, { label: string; badge: BadgeVariant }> = {
  PENDING: { label: 'Confirmado', badge: 'primary' },
  CONFIRMED: { label: 'Confirmado', badge: 'primary' },
  COMPLETED: { label: 'Concluído', badge: 'success' },
  NO_SHOW: { label: 'Não compareceu', badge: 'neutral' },
  CANCELLED: { label: 'Cancelado', badge: 'danger' }
};

const longDate = new Intl.DateTimeFormat('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' });

@Component({
  selector: 'app-client-appointments',
  imports: [
    FormsModule,
    RouterLink,
    PublicShell,
    Avatar,
    Badge,
    Button,
    Card,
    EmptyState,
    Icon,
    OtpInput,
    Sheet,
    Spinner,
    TextField
  ],
  templateUrl: './client-appointments.html'
})
export class ClientAppointments {
  private readonly clientAuth = inject(ClientAuthService);
  private readonly appointmentService = inject(AppointmentService);
  private readonly waitingListService = inject(WaitingListService);
  private readonly session = inject(SessionStore);
  private readonly toast = inject(ToastService);

  protected readonly phase = signal<Phase>('checking');
  protected readonly tab = signal<Tab>('upcoming');
  protected readonly clientName = signal<string | null>(null);
  protected readonly phone = signal('');
  protected readonly otp = signal('');
  protected readonly otpError = signal<string | null>(null);
  protected readonly sendingCode = signal(false);
  protected readonly verifying = signal(false);
  protected readonly resendIn = signal(0);

  protected readonly appointments = signal<AppointmentResponse[]>([]);
  protected readonly loading = signal(false);
  protected readonly failed = signal(false);
  protected readonly cancelTarget = signal<AppointmentResponse | null>(null);
  protected readonly cancelling = signal(false);

  protected readonly waitlist = signal<WaitingListEntry[]>([]);
  protected readonly leaveTarget = signal<WaitingListEntry | null>(null);
  protected readonly leaving = signal(false);

  protected readonly editingName = signal(false);
  protected readonly nameInput = signal('');
  protected readonly savingName = signal(false);

  protected readonly status = STATUS;
  protected readonly formatPrice = formatPrice;
  protected readonly formatWaitPeriod = formatWaitPeriod;
  protected readonly maskPhone = maskPhoneBR;

  protected readonly canRequestCode = computed(() => digitsOnly(this.phone()).length >= 10);
  protected readonly firstName = computed(() => this.clientName()?.trim().split(' ')[0] ?? '');
  protected readonly canSaveName = computed(() => {
    const name = this.nameInput().trim();
    return name.length > 0 && name !== (this.clientName() ?? '').trim();
  });

  protected readonly upcoming = computed(() =>
    this.appointments()
      .filter((a) => OPEN_STATUSES.includes(a.status) && !isPast(a))
      .sort((a, b) => key(a).localeCompare(key(b)))
  );

  protected readonly history = computed(() =>
    this.appointments()
      .filter((a) => !OPEN_STATUSES.includes(a.status) || isPast(a))
      .sort((a, b) => key(b).localeCompare(key(a)))
  );

  protected readonly visible = computed(() => (this.tab() === 'upcoming' ? this.upcoming() : this.history()));

  private countdown: ReturnType<typeof setInterval> | null = null;

  constructor() {
    inject(DestroyRef).onDestroy(() => this.stopCountdown());

    if (!this.session.clientToken) {
      this.phase.set('phone');
      return;
    }
    this.clientAuth.me().subscribe({
      next: (client) => {
        this.clientName.set(client.name);
        this.phase.set('list');
        this.load();
      },
      error: () => {
        this.session.clearClientSession();
        this.phase.set('phone');
      }
    });
  }

  protected requestCode(): void {
    if (!this.canRequestCode() || this.sendingCode()) return;

    this.sendingCode.set(true);
    this.otpError.set(null);
    this.clientAuth.requestOtp(digitsOnly(this.phone())).subscribe({
      next: () => {
        this.sendingCode.set(false);
        this.otp.set('');
        this.phase.set('code');
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
    this.clientAuth.verifyOtp(digitsOnly(this.phone()), this.otp()).subscribe({
      next: (response) => {
        this.verifying.set(false);
        this.stopCountdown();
        this.clientName.set(response.name);
        this.phase.set('list');
        this.load();
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
    this.phase.set('phone');
  }

  protected logout(): void {
    this.clientAuth.logout().subscribe({
      complete: () => this.reset(),
      error: () => this.reset()
    });
  }

  protected openNameEditor(): void {
    this.nameInput.set(this.clientName() ?? '');
    this.editingName.set(true);
  }

  protected saveName(): void {
    if (!this.canSaveName() || this.savingName()) return;

    this.savingName.set(true);
    this.clientAuth.updateName(this.nameInput().trim()).subscribe({
      next: (client) => {
        this.savingName.set(false);
        this.clientName.set(client.name);
        this.editingName.set(false);
        this.toast.success('Nome atualizado.');
      },
      error: (err: HttpErrorResponse) => {
        this.savingName.set(false);
        if (err.status === 401) {
          this.editingName.set(false);
          this.reset();
        } else if (err.status > 0 && err.status < 500) {
          this.toast.error(apiErrorMessage(err, 'Não foi possível salvar o nome.'));
        }
      }
    });
  }

  protected load(): void {
    this.loading.set(true);
    this.failed.set(false);
    forkJoin([this.appointmentService.listMine(), this.waitingListService.listMine()]).subscribe({
      next: ([appointments, waitlist]) => {
        this.appointments.set(appointments);
        this.waitlist.set(waitlist);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        if (err.status === 401) {
          this.reset();
        } else {
          this.failed.set(true);
        }
      }
    });
  }

  protected confirmCancel(): void {
    const target = this.cancelTarget();
    if (!target || this.cancelling()) return;

    this.cancelling.set(true);
    this.appointmentService.cancel(target.id).subscribe({
      next: (updated) => {
        this.cancelling.set(false);
        this.cancelTarget.set(null);
        this.appointments.update((list) => list.map((a) => (a.id === updated.id ? updated : a)));
        this.toast.success('Agendamento cancelado.');
      },
      error: (err: HttpErrorResponse) => {
        this.cancelling.set(false);
        if (err.status > 0 && err.status < 500) {
          this.toast.error(apiErrorMessage(err, 'Não foi possível cancelar o agendamento.'));
          this.cancelTarget.set(null);
          this.load();
        }
      }
    });
  }

  protected setCancelOpen(open: boolean): void {
    if (!open) this.cancelTarget.set(null);
  }

  protected confirmLeave(): void {
    const target = this.leaveTarget();
    if (!target || this.leaving()) return;

    this.leaving.set(true);
    this.waitingListService.leave(target.id).subscribe({
      next: () => {
        this.leaving.set(false);
        this.leaveTarget.set(null);
        this.waitlist.update((list) => list.filter((entry) => entry.id !== target.id));
        this.toast.success('Você saiu da fila.');
      },
      error: (err: HttpErrorResponse) => {
        this.leaving.set(false);
        if (err.status > 0 && err.status < 500) {
          this.toast.error(apiErrorMessage(err, 'Não foi possível sair da fila.'));
          this.leaveTarget.set(null);
          this.load();
        }
      }
    });
  }

  protected setLeaveOpen(open: boolean): void {
    if (!open) this.leaveTarget.set(null);
  }

  protected describeOffer(entry: WaitingListEntry): string {
    if (!entry.offered_date || !entry.offered_time) return '';
    const label = longDate.format(new Date(`${entry.offered_date}T12:00:00`));
    return `${label}, às ${entry.offered_time}`;
  }

  protected describeDate(appointment: AppointmentResponse): string {
    const label = longDate.format(new Date(`${appointment.scheduled_date}T12:00:00`));
    return label.charAt(0).toUpperCase() + label.slice(1);
  }

  protected timeRange(appointment: AppointmentResponse): string {
    const [h, m] = appointment.scheduled_time.split(':').map(Number);
    const end = h * 60 + m + appointment.duration_minutes;
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${appointment.scheduled_time} – ${pad(Math.floor(end / 60) % 24)}:${pad(end % 60)}`;
  }

  private reset(): void {
    this.session.clearClientSession();
    this.appointments.set([]);
    this.waitlist.set([]);
    this.clientName.set(null);
    this.phone.set('');
    this.otp.set('');
    this.tab.set('upcoming');
    this.phase.set('phone');
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

function key(appointment: AppointmentResponse): string {
  return `${appointment.scheduled_date}T${appointment.scheduled_time}`;
}

function isPast(appointment: AppointmentResponse): boolean {
  const now = new Date();
  const local = new Date(now.getTime() - now.getTimezoneOffset() * 60_000).toISOString().slice(0, 16);
  return key(appointment) <= local;
}
