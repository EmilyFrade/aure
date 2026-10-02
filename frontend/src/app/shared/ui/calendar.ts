import { Component, computed, input, model, OnInit, signal } from '@angular/core';
import { Icon } from './icon';

const WEEKDAYS = ['DOM', 'SEG', 'TER', 'QUA', 'QUI', 'SEX', 'SÁB'];
const monthLabel = new Intl.DateTimeFormat('pt-BR', { month: 'long', year: 'numeric' });

interface CalendarCell {
  iso: string;
  day: number;
  disabled: boolean;
  today: boolean;
}

@Component({
  selector: 'app-calendar',
  imports: [Icon],
  template: `
    <div class="mb-3 flex items-center justify-between">
      <p class="flex items-center gap-2 font-display text-base font-semibold text-slate-800">
        <span class="text-primary-500"><app-icon name="calendar" [size]="18" /></span>
        {{ title() }}
      </p>
      <div class="flex gap-1">
        <button
          type="button"
          class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-500 transition hover:bg-primary-50 disabled:opacity-30"
          aria-label="Mês anterior"
          [disabled]="!canGoBack()"
          (click)="shiftMonth(-1)">
          <app-icon name="chevron-left" [size]="18" />
        </button>
        <button
          type="button"
          class="flex h-8 w-8 items-center justify-center rounded-lg text-slate-500 transition hover:bg-primary-50 disabled:opacity-30"
          aria-label="Próximo mês"
          [disabled]="!canGoForward()"
          (click)="shiftMonth(1)">
          <app-icon name="chevron-right" [size]="18" />
        </button>
      </div>
    </div>

    <div class="grid grid-cols-7 gap-1 text-center">
      @for (weekday of weekdays; track weekday) {
        <span class="pb-1 text-[11px] font-medium text-slate-400">{{ weekday }}</span>
      }
      @for (cell of cells(); track $index) {
        @if (cell) {
          <button
            type="button"
            class="relative flex aspect-square items-center justify-center rounded-xl text-sm font-medium transition"
            [class]="
              cell.iso === selected()
                ? 'bg-primary-500 text-white shadow-md shadow-primary-500/30'
                : cell.disabled
                  ? 'cursor-default text-slate-300'
                  : 'text-slate-700 hover:bg-primary-50'
            "
            [disabled]="cell.disabled"
            [attr.aria-pressed]="cell.iso === selected()"
            [attr.aria-label]="cell.iso"
            (click)="selected.set(cell.iso)">
            {{ cell.day }}
            @if (cell.today && cell.iso !== selected()) {
              <span class="absolute bottom-1 h-1 w-1 rounded-full bg-blush-400"></span>
            }
          </button>
        } @else {
          <span></span>
        }
      }
    </div>
  `,
  styles: `:host { display: block; }`
})
export class Calendar implements OnInit {
  readonly selected = model('');
  readonly min = input('');
  readonly max = input('');

  protected readonly weekdays = WEEKDAYS;
  private readonly month = signal('');

  ngOnInit(): void {
    this.month.set((this.selected() || this.min() || isoToday()).slice(0, 7));
  }

  protected readonly title = computed(() => {
    const label = monthLabel.format(parseIso(`${this.month()}-01`));
    return label.charAt(0).toUpperCase() + label.slice(1);
  });

  protected readonly canGoBack = computed(() => !this.min() || this.month() > this.min().slice(0, 7));
  protected readonly canGoForward = computed(() => !this.max() || this.month() < this.max().slice(0, 7));

  protected readonly cells = computed<(CalendarCell | null)[]>(() => {
    if (!this.month()) return [];
    const first = parseIso(`${this.month()}-01`);
    const daysInMonth = new Date(first.getFullYear(), first.getMonth() + 1, 0).getDate();
    const today = isoToday();
    const cells: (CalendarCell | null)[] = Array.from({ length: first.getDay() }, () => null);
    for (let day = 1; day <= daysInMonth; day++) {
      const iso = `${this.month()}-${String(day).padStart(2, '0')}`;
      cells.push({
        iso,
        day,
        today: iso === today,
        disabled: (!!this.min() && iso < this.min()) || (!!this.max() && iso > this.max())
      });
    }
    return cells;
  });

  protected shiftMonth(delta: number): void {
    const date = parseIso(`${this.month()}-01`);
    date.setMonth(date.getMonth() + delta);
    this.month.set(toIso(date).slice(0, 7));
  }
}

function parseIso(iso: string): Date {
  return new Date(`${iso}T12:00:00`);
}

function toIso(date: Date): string {
  return date.toLocaleDateString('sv-SE');
}

function isoToday(): string {
  return toIso(new Date());
}
