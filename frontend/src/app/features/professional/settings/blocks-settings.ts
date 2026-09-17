import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BlockResponse } from '../../../core/models/api.models';
import { ScheduleService } from '../../../core/services/schedule.service';
import { ToastService } from '../../../shared/feedback/toast.service';
import { Button } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { EmptyState } from '../../../shared/ui/empty-state';
import { Icon } from '../../../shared/ui/icon';
import { Spinner } from '../../../shared/ui/spinner';
import { TextField } from '../../../shared/ui/text-field';
import { apiErrorMessage } from '../../../shared/utils/format';

const FULL_DAY_START = '00:00';
const FULL_DAY_END = '23:59';

const dateLabel = new Intl.DateTimeFormat('pt-BR', { weekday: 'short', day: 'numeric', month: 'short' });

@Component({
  selector: 'app-blocks-settings',
  imports: [FormsModule, Button, Card, EmptyState, Icon, Spinner, TextField],
  templateUrl: './blocks-settings.html'
})
export class BlocksSettings {
  private readonly scheduleService = inject(ScheduleService);
  private readonly toast = inject(ToastService);

  readonly professionalId = input.required<number>();

  protected readonly blocks = signal<BlockResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly formOpen = signal(false);
  protected readonly saving = signal(false);
  protected readonly confirmDeleteId = signal<number | null>(null);

  protected readonly today = localIso().slice(0, 10);
  protected readonly startDate = signal(this.today);
  protected readonly endDate = signal(this.today);
  protected readonly fullDay = signal(true);
  protected readonly startTime = signal('12:00');
  protected readonly endTime = signal('13:00');
  protected readonly reason = signal('');

  protected readonly upcoming = computed(() => {
    const now = localIso();
    return this.blocks()
      .filter((b) => b.end_datetime >= now)
      .sort((a, b) => a.start_datetime.localeCompare(b.start_datetime));
  });

  private readonly range = computed(() => ({
    start: `${this.startDate()}T${this.fullDay() ? FULL_DAY_START : this.startTime()}`,
    end: `${this.endDate()}T${this.fullDay() ? FULL_DAY_END : this.endTime()}`
  }));

  protected readonly formError = computed(() => {
    if (!this.startDate() || !this.endDate()) return null;
    if (this.endDate() < this.startDate()) return 'A data final não pode ser antes da inicial.';
    const { start, end } = this.range();
    if (start >= end) return 'O término deve ser depois do início.';
    return null;
  });

  protected readonly canSave = computed(
    () =>
      !!this.startDate() &&
      !!this.endDate() &&
      (this.fullDay() || (!!this.startTime() && !!this.endTime())) &&
      !this.formError()
  );

  ngOnInit(): void {
    this.scheduleService.listBlocks(this.professionalId()).subscribe({
      next: (blocks) => {
        this.blocks.set(blocks);
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  protected openForm(): void {
    this.startDate.set(this.today);
    this.endDate.set(this.today);
    this.fullDay.set(true);
    this.reason.set('');
    this.formOpen.set(true);
  }

  protected setStartDate(value: string): void {
    this.startDate.set(value);
    if (value && this.endDate() < value) this.endDate.set(value);
  }

  protected save(): void {
    if (!this.canSave() || this.saving()) return;

    const { start, end } = this.range();
    this.saving.set(true);
    this.scheduleService
      .createBlock(this.professionalId(), {
        start_datetime: start,
        end_datetime: end,
        reason: this.reason().trim() || undefined
      })
      .subscribe({
        next: (block) => {
          this.blocks.update((list) => [...list, block]);
          this.saving.set(false);
          this.formOpen.set(false);
          this.toast.success('Bloqueio adicionado.');
        },
        error: (err: HttpErrorResponse) => {
          this.saving.set(false);
          if (err.status > 0 && err.status < 500) {
            this.toast.error(apiErrorMessage(err, 'Não foi possível criar o bloqueio.'));
          }
        }
      });
  }

  protected remove(block: BlockResponse): void {
    if (this.confirmDeleteId() !== block.id) {
      this.confirmDeleteId.set(block.id);
      return;
    }
    this.scheduleService.deleteBlock(this.professionalId(), block.id).subscribe({
      next: () => {
        this.blocks.update((list) => list.filter((b) => b.id !== block.id));
        this.confirmDeleteId.set(null);
        this.toast.success('Bloqueio removido.');
      },
      error: () => this.confirmDeleteId.set(null)
    });
  }

  protected describe(block: BlockResponse): { title: string; detail: string } {
    const [startDate, startTime] = block.start_datetime.split('T');
    const [endDate, endTime] = block.end_datetime.split('T');
    const fullDay = startTime === FULL_DAY_START && endTime === FULL_DAY_END;
    const d1 = formatDate(startDate);
    const d2 = formatDate(endDate);

    if (startDate === endDate) {
      return { title: d1, detail: fullDay ? 'Dia inteiro' : `${startTime} – ${endTime}` };
    }
    return fullDay
      ? { title: `${d1} → ${d2}`, detail: 'Dias inteiros' }
      : { title: `${d1} ${startTime} → ${d2} ${endTime}`, detail: 'Período contínuo' };
  }
}

function formatDate(isoDate: string): string {
  const label = dateLabel.format(new Date(`${isoDate}T12:00:00`));
  return label.charAt(0).toUpperCase() + label.slice(1);
}

function localIso(): string {
  const now = new Date();
  return new Date(now.getTime() - now.getTimezoneOffset() * 60_000).toISOString().slice(0, 16);
}
