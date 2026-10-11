import { HttpErrorResponse } from '@angular/common/http';
import { DayOfWeek, DayPeriod } from '../../core/models/api.models';

export const WEEK_DAYS: readonly { value: DayOfWeek; short: string; label: string }[] = [
  { value: 'MONDAY', short: 'Seg', label: 'Segunda' },
  { value: 'TUESDAY', short: 'Ter', label: 'Terça' },
  { value: 'WEDNESDAY', short: 'Qua', label: 'Quarta' },
  { value: 'THURSDAY', short: 'Qui', label: 'Quinta' },
  { value: 'FRIDAY', short: 'Sex', label: 'Sexta' },
  { value: 'SATURDAY', short: 'Sáb', label: 'Sábado' },
  { value: 'SUNDAY', short: 'Dom', label: 'Domingo' }
];

const currency = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

export function formatPrice(value: number): string {
  return currency.format(value);
}

export function formatDuration(minutes: number): string {
  if (minutes < 60) return `${minutes} min`;
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return m ? `${h}h${String(m).padStart(2, '0')}` : `${h}h`;
}

export function apiErrorMessage(err: HttpErrorResponse, fallback: string): string {
  return err.error?.detail ?? fallback;
}

const dayMonth = new Intl.DateTimeFormat('pt-BR', { day: 'numeric', month: 'short' });

export function formatDateRange(start: string, end: string): string {
  const format = (iso: string) => dayMonth.format(new Date(`${iso}T12:00:00`)).replace('.', '');
  return start === end ? `Só em ${format(start)}` : `De ${format(start)} a ${format(end)}`;
}

export const DAY_PERIODS: readonly { value: DayPeriod | null; label: string }[] = [
  { value: null, label: 'Qualquer' },
  { value: 'MORNING', label: 'Manhã' },
  { value: 'AFTERNOON', label: 'Tarde' },
  { value: 'EVENING', label: 'Noite' }
];

export function formatWaitPeriod(start: string, end: string, period: DayPeriod | null | undefined): string {
  const range = formatDateRange(start, end);
  const label = DAY_PERIODS.find((p) => p.value === period && period);
  return label ? `${range} · ${label.label}` : range;
}
