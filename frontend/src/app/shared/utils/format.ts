import { HttpErrorResponse } from '@angular/common/http';
import { DayOfWeek } from '../../core/models/api.models';

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
