import { HttpErrorResponse } from '@angular/common/http';
import { Component, HostListener, computed, inject, signal } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ServiceResponse, WaitingListEntry } from '../../../core/models/api.models';
import { ProfessionalAuthService } from '../../../core/services/professional-auth.service';
import { ServiceCatalogService } from '../../../core/services/service-catalog.service';
import { WaitingListService } from '../../../core/services/waiting-list.service';
import { ToastService } from '../../../shared/feedback/toast.service';
import { AppShell } from '../../../shared/layout/app-shell';
import { Avatar } from '../../../shared/ui/avatar';
import { Button } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { EmptyState } from '../../../shared/ui/empty-state';
import { Icon } from '../../../shared/ui/icon';
import { Sheet } from '../../../shared/ui/sheet';
import { Spinner } from '../../../shared/ui/spinner';
import { apiErrorMessage, formatWaitPeriod } from '../../../shared/utils/format';
import { clientLabel, describeShort, isoToday } from '../agenda/agenda-utils';
import { NewAppointmentForm } from '../agenda/new-appointment-form';

@Component({
  selector: 'app-professional-waiting-list',
  imports: [NgTemplateOutlet, FormsModule, AppShell, Avatar, Button, Card, EmptyState, Icon, NewAppointmentForm, Sheet, Spinner],
  templateUrl: './professional-waiting-list.html'
})
export class ProfessionalWaitingList {
  private readonly waitingListService = inject(WaitingListService);
  private readonly serviceCatalog = inject(ServiceCatalogService);
  private readonly toast = inject(ToastService);

  protected readonly professionalId = inject(ProfessionalAuthService).professionalId!;

  protected readonly entries = signal<WaitingListEntry[]>([]);
  protected readonly services = signal<ServiceResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly failed = signal(false);
  protected readonly search = signal('');
  protected readonly statusFilter = signal<'ALL' | 'WAITING' | 'NOTIFIED'>('ALL');
  protected readonly menuFor = signal<number | null>(null);

  protected readonly bookTarget = signal<WaitingListEntry | null>(null);
  protected readonly removeTarget = signal<WaitingListEntry | null>(null);
  protected readonly removing = signal(false);

  protected readonly clientLabel = clientLabel;
  protected readonly formatWaitPeriod = formatWaitPeriod;

  protected readonly waitingCount = computed(() => this.entries().filter((e) => e.status === 'WAITING').length);
  protected readonly notifiedCount = computed(() => this.entries().filter((e) => e.status === 'NOTIFIED').length);

  protected readonly visible = computed(() => {
    const status = this.statusFilter();
    const term = normalize(this.search());
    return this.entries().filter(
      (e) =>
        (status === 'ALL' || e.status === status) &&
        (!term || normalize(`${e.client_name ?? ''} ${e.client_phone} ${e.service_name}`).includes(term))
    );
  });

  constructor() {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.failed.set(false);
    forkJoin({
      entries: this.waitingListService.listForProfessional(this.professionalId),
      services: this.serviceCatalog.list(this.professionalId)
    }).subscribe({
      next: ({ entries, services }) => {
        this.entries.set(entries);
        this.services.set(services.filter((s) => s.is_active));
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.failed.set(true);
      }
    });
  }

  protected readonly statusOptions = computed(() => [
    { value: 'ALL' as const, label: 'Todos', count: this.entries().length },
    { value: 'WAITING' as const, label: 'Aguardando', count: this.waitingCount() },
    { value: 'NOTIFIED' as const, label: 'Avisados', count: this.notifiedCount() }
  ]);

  @HostListener('document:click')
  protected closeMenu(): void {
    this.menuFor.set(null);
  }

  protected toggleMenu(event: MouseEvent, id: number): void {
    event.stopPropagation();
    this.menuFor.update((current) => (current === id ? null : id));
  }

  protected bookDate(entry: WaitingListEntry): string {
    if (entry.offered_date && entry.offered_date >= isoToday()) return entry.offered_date;
    return entry.preferred_date_start > isoToday() ? entry.preferred_date_start : isoToday();
  }

  protected waitingFor(entry: WaitingListEntry): string {
    const days = daysSince(entry.created_at);
    if (days === 0) return 'Entrou hoje';
    if (days === 1) return 'Entrou ontem';
    return `Entrou há ${days} dias`;
  }

  protected describeOffer(entry: WaitingListEntry): string {
    return entry.offered_date ? `${describeShort(entry.offered_date)}, ${entry.offered_time}` : '';
  }

  protected onBooked(): void {
    this.bookTarget.set(null);
    this.load();
  }

  protected setBookOpen(open: boolean): void {
    if (!open) this.bookTarget.set(null);
  }

  protected confirmRemove(): void {
    const target = this.removeTarget();
    if (!target || this.removing()) return;

    this.removing.set(true);
    this.waitingListService.remove(this.professionalId, target.id).subscribe({
      next: () => {
        this.removing.set(false);
        this.removeTarget.set(null);
        this.toast.success('Cliente removido da fila.');
        this.load();
      },
      error: (err: HttpErrorResponse) => {
        this.removing.set(false);
        if (err.status > 0 && err.status < 500) {
          this.toast.error(apiErrorMessage(err, 'Não foi possível remover da fila.'));
        }
      }
    });
  }

  protected setRemoveOpen(open: boolean): void {
    if (!open) this.removeTarget.set(null);
  }
}

function daysSince(iso: string): number {
  const start = new Date(iso);
  start.setHours(0, 0, 0, 0);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return Math.max(0, Math.round((today.getTime() - start.getTime()) / 86_400_000));
}

function normalize(text: string): string {
  return text.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
}
