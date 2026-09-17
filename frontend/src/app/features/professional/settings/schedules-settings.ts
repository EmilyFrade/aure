import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DayOfWeek, ScheduleResponse } from '../../../core/models/api.models';
import { ScheduleService } from '../../../core/services/schedule.service';
import { ToastService } from '../../../shared/feedback/toast.service';
import { Button } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { Icon } from '../../../shared/ui/icon';
import { Spinner } from '../../../shared/ui/spinner';
import { apiErrorMessage, WEEK_DAYS } from '../../../shared/utils/format';

interface Editor {
  day: DayOfWeek;
  id: number | null;
  start: string;
  end: string;
}

@Component({
  selector: 'app-schedules-settings',
  imports: [FormsModule, Button, Card, Icon, Spinner],
  templateUrl: './schedules-settings.html'
})
export class SchedulesSettings {
  private readonly scheduleService = inject(ScheduleService);
  private readonly toast = inject(ToastService);

  readonly professionalId = input.required<number>();

  protected readonly schedules = signal<ScheduleResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly editor = signal<Editor | null>(null);

  protected readonly days = computed(() =>
    WEEK_DAYS.map((day) => ({
      ...day,
      items: this.schedules()
        .filter((s) => s.day_of_week === day.value)
        .sort((a, b) => a.start_time.localeCompare(b.start_time))
    }))
  );

  protected readonly editorError = computed(() => {
    const e = this.editor();
    if (!e || !e.start || !e.end) return null;
    if (e.start >= e.end) return 'O término deve ser depois do início.';
    const overlaps = this.schedules().some(
      (s) => s.day_of_week === e.day && s.id !== e.id && e.start < s.end_time && e.end > s.start_time
    );
    return overlaps ? 'Esse intervalo se sobrepõe a outro horário do dia.' : null;
  });

  ngOnInit(): void {
    this.scheduleService.list(this.professionalId()).subscribe({
      next: (schedules) => {
        this.schedules.set(schedules);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  protected openAdd(day: DayOfWeek, items: ScheduleResponse[]): void {
    const last = items.at(-1);
    this.editor.set(
      last
        ? { day, id: null, start: last.end_time, end: addHours(last.end_time, 4) }
        : { day, id: null, start: '09:00', end: '18:00' }
    );
  }

  protected openEdit(item: ScheduleResponse): void {
    this.editor.set({ day: item.day_of_week, id: item.id, start: item.start_time, end: item.end_time });
  }

  protected patchEditor(field: 'start' | 'end', value: string): void {
    this.editor.update((e) => (e ? { ...e, [field]: value } : e));
  }

  protected save(): void {
    const e = this.editor();
    if (!e || !e.start || !e.end || this.editorError() || this.saving()) return;

    const request = { day_of_week: e.day, start_time: e.start, end_time: e.end };
    const call = e.id
      ? this.scheduleService.update(this.professionalId(), e.id, request)
      : this.scheduleService.create(this.professionalId(), request);

    this.saving.set(true);
    call.subscribe({
      next: (saved) => {
        this.schedules.update((list) => [...list.filter((s) => s.id !== saved.id), saved]);
        this.saving.set(false);
        this.editor.set(null);
        this.toast.success('Horário salvo.');
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        if (err.status > 0 && err.status < 500) {
          this.toast.error(apiErrorMessage(err, 'Não foi possível salvar o horário.'));
        }
      }
    });
  }

  protected remove(item: ScheduleResponse): void {
    this.scheduleService.delete(this.professionalId(), item.id).subscribe({
      next: () => {
        this.schedules.update((list) => list.filter((s) => s.id !== item.id));
        if (this.editor()?.id === item.id) this.editor.set(null);
        this.toast.success('Intervalo removido.');
      },
      error: (err: HttpErrorResponse) => {
        if (err.status > 0 && err.status < 500) {
          this.toast.error(apiErrorMessage(err, 'Não foi possível remover o intervalo.'));
        }
      }
    });
  }
}

function addHours(time: string, hours: number): string {
  const [h, m] = time.split(':').map(Number);
  const total = Math.min(h * 60 + m + hours * 60, 23 * 60 + 59);
  return `${String(Math.floor(total / 60)).padStart(2, '0')}:${String(total % 60).padStart(2, '0')}`;
}
