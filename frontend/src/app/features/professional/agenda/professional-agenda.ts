import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import {
  AppointmentResponse,
  AppointmentStatus,
  BlockResponse,
  ScheduleResponse,
  ServiceResponse
} from '../../../core/models/api.models';
import { ProfessionalAppointmentService } from '../../../core/services/professional-appointment.service';
import { ProfessionalAuthService } from '../../../core/services/professional-auth.service';
import { ScheduleService } from '../../../core/services/schedule.service';
import { ServiceCatalogService } from '../../../core/services/service-catalog.service';
import { ToastService } from '../../../shared/feedback/toast.service';
import { AppShell } from '../../../shared/layout/app-shell';
import { Button } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { EmptyState } from '../../../shared/ui/empty-state';
import { Icon } from '../../../shared/ui/icon';
import { Sheet } from '../../../shared/ui/sheet';
import { Spinner } from '../../../shared/ui/spinner';
import { apiErrorMessage, formatPrice } from '../../../shared/utils/format';
import {
  STATUS_BORDER,
  STATUS_DOT,
  STATUS_LABEL,
  addDays,
  clientLabel,
  dayOfWeek,
  describeLong,
  describeShort,
  describeWeekday,
  endTime,
  fromMinutes,
  isoToday,
  nowMinutes,
  parseIso,
  startOfWeek,
  toMinutes
} from './agenda-utils';
import { AppointmentCard } from './appointment-card';
import { NewAppointmentForm } from './new-appointment-form';

type ViewMode = 'day' | 'week';

type TimelineRow =
  | { kind: 'appointments'; key: string; items: AppointmentResponse[] }
  | { kind: 'free'; key: string; hour: string }
  | { kind: 'blocked'; key: string; from: number; to: number; reason: string | null }
  | { kind: 'off'; key: string; from: number; to: number };

interface WeekDay {
  iso: string;
  weekday: string;
  day: number;
  isToday: boolean;
  works: boolean;
  appointments: AppointmentResponse[];
}

const ACTIVE_STATUSES: readonly AppointmentStatus[] = ['PENDING', 'CONFIRMED', 'COMPLETED', 'NO_SHOW'];
const REVENUE_STATUSES: readonly AppointmentStatus[] = ['PENDING', 'CONFIRMED', 'COMPLETED'];

@Component({
  selector: 'app-professional-agenda',
  imports: [AppShell, AppointmentCard, Button, Card, EmptyState, Icon, NewAppointmentForm, Sheet, Spinner],
  templateUrl: './professional-agenda.html'
})
export class ProfessionalAgenda {
  private readonly appointmentService = inject(ProfessionalAppointmentService);
  private readonly scheduleService = inject(ScheduleService);
  private readonly serviceCatalog = inject(ServiceCatalogService);
  private readonly toast = inject(ToastService);

  protected readonly professionalId = inject(ProfessionalAuthService).professionalId!;

  protected readonly view = signal<ViewMode>('day');
  protected readonly date = signal(isoToday());
  protected readonly appointments = signal<AppointmentResponse[]>([]);
  protected readonly schedules = signal<ScheduleResponse[]>([]);
  protected readonly blocks = signal<BlockResponse[]>([]);
  protected readonly services = signal<ServiceResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly failed = signal(false);
  protected readonly busyId = signal<number | null>(null);

  protected readonly newOpen = signal(false);
  protected readonly newDate = signal(isoToday());
  protected readonly newTime = signal<string | null>(null);
  protected readonly cancelTarget = signal<AppointmentResponse | null>(null);
  protected readonly cancelling = signal(false);

  protected readonly statusLabel = STATUS_LABEL;
  protected readonly statusBorder = STATUS_BORDER;
  protected readonly statusDot = STATUS_DOT;
  protected readonly clientLabel = clientLabel;
  protected readonly endTime = endTime;
  protected readonly describeLong = describeLong;
  protected readonly describeShort = describeShort;
  protected readonly fromMinutes = fromMinutes;

  protected readonly weekStart = computed(() => startOfWeek(this.date()));
  protected readonly isToday = computed(() => this.date() === isoToday());

  protected readonly week = computed<WeekDay[]>(() => {
    const today = isoToday();
    return Array.from({ length: 7 }, (_, i) => {
      const iso = addDays(this.weekStart(), i);
      return {
        iso,
        weekday: describeWeekday(iso),
        day: parseIso(iso).getDate(),
        isToday: iso === today,
        works: this.windowsFor(iso).length > 0,
        appointments: this.appointments().filter((a) => a.scheduled_date === iso && a.status !== 'CANCELLED')
      };
    });
  });

  protected readonly rangeLabel = computed(() => {
    if (this.view() === 'day') return describeLong(this.date());
    const end = addDays(this.weekStart(), 6);
    return `${describeShort(this.weekStart())} – ${describeShort(end)}`;
  });

  protected readonly dayAppointments = computed(() =>
    this.appointments().filter((a) => a.scheduled_date === this.date())
  );

  protected readonly activeToday = computed(() =>
    this.dayAppointments().filter((a) => ACTIVE_STATUSES.includes(a.status))
  );

  protected readonly cancelledToday = computed(() =>
    this.dayAppointments().filter((a) => a.status === 'CANCELLED')
  );

  protected readonly expectedRevenue = computed(() =>
    formatPrice(
      this.dayAppointments()
        .filter((a) => REVENUE_STATUSES.includes(a.status))
        .reduce((sum, a) => sum + Number(a.price), 0)
    )
  );

  protected readonly pendingCount = computed(
    () => this.dayAppointments().filter((a) => a.status === 'PENDING').length
  );

  protected readonly next = computed(() => {
    if (!this.isToday()) return null;
    const now = nowMinutes();
    return (
      this.dayAppointments().find(
        (a) => (a.status === 'PENDING' || a.status === 'CONFIRMED') && toMinutes(a.scheduled_time) >= now
      ) ?? null
    );
  });

  protected readonly timeline = computed<TimelineRow[]>(() => {
    const date = this.date();
    const windows = this.windowsFor(date);
    const items = this.activeToday();
    if (windows.length === 0 && items.length === 0) return [];

    const starts = [...windows.map((w) => w.start), ...items.map((a) => toMinutes(a.scheduled_time))];
    const ends = [...windows.map((w) => w.end), ...items.map((a) => toMinutes(a.scheduled_time) + a.duration_minutes)];
    const first = Math.floor(Math.min(...starts) / 60);
    const last = Math.ceil(Math.max(...ends) / 60);
    const pastLimit = date < isoToday() ? 24 * 60 : date === isoToday() ? nowMinutes() : -1;
    const dayBlocks = this.blocksFor(date);

    const rows: TimelineRow[] = [];
    for (let h = first; h < last; h++) {
      const from = h * 60;
      const to = from + 60;
      const hour = fromMinutes(from);
      const inHour = items.filter((a) => Math.floor(toMinutes(a.scheduled_time) / 60) === h);
      if (inHour.length > 0) {
        rows.push({ kind: 'appointments', key: hour, items: inHour });
        continue;
      }
      if (items.some((a) => {
        const start = toMinutes(a.scheduled_time);
        return start < from && start + a.duration_minutes >= to;
      })) {
        continue;
      }
      const previous = rows.at(-1);
      const block = dayBlocks.find((b) => b.start < to && b.end > from);
      if (block) {
        const end = Math.min(block.end, to);
        if (previous?.kind === 'blocked' && previous.reason === block.reason && previous.to >= Math.max(block.start, from)) {
          previous.to = end;
        } else {
          rows.push({ kind: 'blocked', key: hour, from: Math.max(block.start, from), to: end, reason: block.reason });
        }
      } else if (!windows.some((w) => w.start < to && w.end > from)) {
        if (previous?.kind === 'off') {
          previous.to = to;
        } else {
          rows.push({ kind: 'off', key: hour, from, to });
        }
      } else if (to > pastLimit) {
        rows.push({ kind: 'free', key: hour, hour });
      }
    }
    return rows;
  });

  constructor() {
    forkJoin({
      schedules: this.scheduleService.list(this.professionalId),
      blocks: this.scheduleService.listBlocks(this.professionalId),
      services: this.serviceCatalog.list(this.professionalId)
    }).subscribe({
      next: ({ schedules, blocks, services }) => {
        this.schedules.set(schedules);
        this.blocks.set(blocks);
        this.services.set(services.filter((s) => s.is_active));
      },
      error: () => this.failed.set(true)
    });

    effect(() => this.loadWeek(this.weekStart()));
  }

  protected setView(mode: ViewMode): void {
    this.view.set(mode);
  }

  protected shift(direction: 1 | -1): void {
    this.date.update((d) => addDays(d, direction * (this.view() === 'day' ? 1 : 7)));
  }

  protected goToday(): void {
    this.date.set(isoToday());
  }

  protected openDay(iso: string): void {
    this.date.set(iso);
    this.view.set('day');
  }

  protected openNew(date = this.date(), time: string | null = null): void {
    this.newDate.set(date);
    this.newTime.set(time);
    this.newOpen.set(true);
  }

  protected onCreated(appointment: AppointmentResponse): void {
    this.newOpen.set(false);
    this.view.set('day');
    if (this.date() === appointment.scheduled_date) {
      this.loadWeek(this.weekStart());
    } else {
      this.date.set(appointment.scheduled_date);
    }
  }

  protected updateStatus(appointment: AppointmentResponse, status: AppointmentStatus): void {
    this.busyId.set(appointment.id);
    this.appointmentService.updateStatus(this.professionalId, appointment.id, status).subscribe({
      next: (updated) => {
        this.busyId.set(null);
        this.replace(updated);
      },
      error: (err: HttpErrorResponse) => {
        this.busyId.set(null);
        if (err.status > 0 && err.status < 500) {
          this.toast.error(apiErrorMessage(err, 'Não foi possível atualizar o agendamento.'));
        }
      }
    });
  }

  protected confirmCancel(): void {
    const target = this.cancelTarget();
    if (!target || this.cancelling()) return;

    this.cancelling.set(true);
    this.appointmentService.cancel(this.professionalId, target.id).subscribe({
      next: (updated) => {
        this.cancelling.set(false);
        this.cancelTarget.set(null);
        this.replace(updated);
        this.toast.success('Agendamento cancelado.');
      },
      error: (err: HttpErrorResponse) => {
        this.cancelling.set(false);
        if (err.status > 0 && err.status < 500) {
          this.toast.error(apiErrorMessage(err, 'Não foi possível cancelar o agendamento.'));
        }
      }
    });
  }

  protected setCancelOpen(open: boolean): void {
    if (!open) this.cancelTarget.set(null);
  }

  protected reload(): void {
    this.loadWeek(this.weekStart());
  }

  private loadWeek(start: string): void {
    this.loading.set(true);
    this.failed.set(false);
    this.appointmentService
      .list(this.professionalId, { startDate: start, endDate: addDays(start, 6) })
      .subscribe({
        next: (list) => {
          this.appointments.set(
            [...list].sort((a, b) =>
              `${a.scheduled_date}${a.scheduled_time}`.localeCompare(`${b.scheduled_date}${b.scheduled_time}`)
            )
          );
          this.loading.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.failed.set(true);
        }
      });
  }

  private replace(updated: AppointmentResponse): void {
    this.appointments.update((list) => list.map((a) => (a.id === updated.id ? updated : a)));
  }

  private windowsFor(iso: string): { start: number; end: number }[] {
    const day = dayOfWeek(iso);
    return this.schedules()
      .filter((s) => s.day_of_week === day)
      .map((s) => ({ start: toMinutes(s.start_time), end: toMinutes(s.end_time) }));
  }

  private blocksFor(iso: string): { start: number; end: number; reason: string | null }[] {
    const dayStart = `${iso}T00:00`;
    const dayEnd = `${iso}T23:59`;
    return this.blocks()
      .filter((b) => b.start_datetime <= dayEnd && b.end_datetime >= dayStart)
      .map((b) => {
        const start = b.start_datetime < dayStart ? 0 : toMinutes(b.start_datetime.slice(11));
        const end = b.end_datetime >= dayEnd ? 24 * 60 : toMinutes(b.end_datetime.slice(11));
        return { start, end, reason: b.reason };
      });
  }}
