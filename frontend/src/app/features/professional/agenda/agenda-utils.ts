import { AppointmentResponse, AppointmentStatus, DayOfWeek } from '../../../core/models/api.models';
import { BadgeVariant } from '../../../shared/ui/badge';
import { maskPhoneBR } from '../../../shared/utils/masks';

export const WEEK_DAYS: readonly DayOfWeek[] = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY'
];

export const STATUS_LABEL: Record<AppointmentStatus, string> = {
  PENDING: 'Pendente',
  CONFIRMED: 'Confirmado',
  COMPLETED: 'Concluído',
  NO_SHOW: 'Não veio',
  CANCELLED: 'Cancelado'
};

export const STATUS_BADGE: Record<AppointmentStatus, BadgeVariant> = {
  PENDING: 'warning',
  CONFIRMED: 'primary',
  COMPLETED: 'success',
  NO_SHOW: 'neutral',
  CANCELLED: 'danger'
};

export const STATUS_BORDER: Record<AppointmentStatus, string> = {
  PENDING: 'border-l-accent-400',
  CONFIRMED: 'border-l-primary-400',
  COMPLETED: 'border-l-emerald-400',
  NO_SHOW: 'border-l-slate-300',
  CANCELLED: 'border-l-red-300'
};

export const STATUS_DOT: Record<AppointmentStatus, string> = {
  PENDING: 'bg-accent-400',
  CONFIRMED: 'bg-primary-400',
  COMPLETED: 'bg-emerald-400',
  NO_SHOW: 'bg-slate-300',
  CANCELLED: 'bg-red-300'
};

const longDate = new Intl.DateTimeFormat('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' });
const shortDate = new Intl.DateTimeFormat('pt-BR', { day: 'numeric', month: 'short' });
const weekdayShort = new Intl.DateTimeFormat('pt-BR', { weekday: 'short' });

export function parseIso(iso: string): Date {
  return new Date(`${iso}T12:00:00`);
}

export function isoToday(): string {
  return new Date().toLocaleDateString('sv-SE');
}

export function addDays(iso: string, days: number): string {
  const date = parseIso(iso);
  date.setDate(date.getDate() + days);
  return date.toLocaleDateString('sv-SE');
}

export function startOfWeek(iso: string): string {
  const offset = (parseIso(iso).getDay() + 6) % 7;
  return addDays(iso, -offset);
}

export function dayOfWeek(iso: string): DayOfWeek {
  return WEEK_DAYS[(parseIso(iso).getDay() + 6) % 7];
}

export function toMinutes(time: string): number {
  const [h, m] = time.split(':').map(Number);
  return h * 60 + m;
}

export function fromMinutes(minutes: number): string {
  const clamped = Math.min(minutes, 24 * 60 - 1);
  return `${String(Math.floor(clamped / 60)).padStart(2, '0')}:${String(clamped % 60).padStart(2, '0')}`;
}

export function nowMinutes(): number {
  const now = new Date();
  return now.getHours() * 60 + now.getMinutes();
}

export function endTime(appointment: AppointmentResponse): string {
  return fromMinutes(toMinutes(appointment.scheduled_time) + appointment.duration_minutes);
}

export function clientLabel(appointment: AppointmentResponse): string {
  return appointment.client_name?.trim() || maskPhoneBR(appointment.client_phone);
}

export function hasStarted(appointment: AppointmentResponse): boolean {
  const today = isoToday();
  if (appointment.scheduled_date !== today) return appointment.scheduled_date < today;
  return toMinutes(appointment.scheduled_time) <= nowMinutes();
}

export function describeLong(iso: string): string {
  return capitalize(longDate.format(parseIso(iso)));
}

export function describeShort(iso: string): string {
  return shortDate.format(parseIso(iso)).replace('.', '');
}

export function describeWeekday(iso: string): string {
  return capitalize(weekdayShort.format(parseIso(iso)).replace('.', ''));
}

export function capitalize(text: string): string {
  return text.charAt(0).toUpperCase() + text.slice(1);
}
