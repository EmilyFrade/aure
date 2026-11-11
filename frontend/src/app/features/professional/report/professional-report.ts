import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MonthlyReport } from '../../../core/models/api.models';
import { ProfessionalAuthService } from '../../../core/services/professional-auth.service';
import { ReportService } from '../../../core/services/report.service';
import { AppShell } from '../../../shared/layout/app-shell';
import { Button } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { EmptyState } from '../../../shared/ui/empty-state';
import { Icon } from '../../../shared/ui/icon';
import { Spinner } from '../../../shared/ui/spinner';
import { formatPrice } from '../../../shared/utils/format';

@Component({
  selector: 'app-professional-report',
  imports: [RouterLink, AppShell, Button, Card, EmptyState, Icon, Spinner],
  templateUrl: './professional-report.html'
})
export class ProfessionalReport {
  private readonly reportService = inject(ReportService);
  private readonly professionalId = inject(ProfessionalAuthService).professionalId!;

  protected readonly month = signal(currentMonth());
  protected readonly report = signal<MonthlyReport | null>(null);
  protected readonly loading = signal(true);
  protected readonly failed = signal(false);

  protected readonly formatPrice = formatPrice;

  protected readonly monthLabel = computed(() => {
    const [year, month] = this.month().split('-').map(Number);
    const label = new Intl.DateTimeFormat('pt-BR', { month: 'long', year: 'numeric' }).format(new Date(year, month - 1, 1));
    return label.charAt(0).toUpperCase() + label.slice(1);
  });

  protected readonly monthName = computed(() => this.monthLabel().split(' ')[0].toLowerCase());

  protected readonly isEmpty = computed(() => {
    const r = this.report();
    return !!r && r.completed_count + r.upcoming_count + r.unmarked_count + r.no_show_count + r.cancelled_count === 0;
  });

  protected readonly noResponse = computed(() => {
    const r = this.report();
    return r ? r.reminders_sent - r.reminders_confirmed - r.reminders_cancelled : 0;
  });

  constructor() {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.failed.set(false);
    this.reportService.monthly(this.professionalId, this.month()).subscribe({
      next: (report) => {
        this.report.set(report);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.failed.set(true);
      }
    });
  }

  protected shift(months: number): void {
    const [year, month] = this.month().split('-').map(Number);
    const date = new Date(year, month - 1 + months, 1);
    this.month.set(`${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`);
    this.load();
  }

  protected attendance(report: MonthlyReport): string {
    return report.attendance_rate === null ? '—' : `${Math.round(report.attendance_rate * 100)}%`;
  }

  protected share(part: number, total: number): number {
    return total === 0 ? 0 : (part / total) * 100;
  }

  protected plural(count: number, singular: string, pluralForm: string): string {
    return `${count} ${count === 1 ? singular : pluralForm}`;
  }
}

function currentMonth(): string {
  const today = new Date();
  return `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}`;
}
