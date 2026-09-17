import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, input, signal } from '@angular/core';
import { ServiceRequest, ServiceResponse } from '../../../core/models/api.models';
import { ServiceCatalogService } from '../../../core/services/service-catalog.service';
import { ToastService } from '../../../shared/feedback/toast.service';
import { Badge } from '../../../shared/ui/badge';
import { Button } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { EmptyState } from '../../../shared/ui/empty-state';
import { Icon } from '../../../shared/ui/icon';
import { Spinner } from '../../../shared/ui/spinner';
import { apiErrorMessage, formatDuration, formatPrice } from '../../../shared/utils/format';
import { ServiceForm } from '../shared/service-form';

@Component({
  selector: 'app-services-settings',
  imports: [Badge, Button, Card, EmptyState, Icon, Spinner, ServiceForm],
  templateUrl: './services-settings.html'
})
export class ServicesSettings {
  private readonly catalog = inject(ServiceCatalogService);
  private readonly toast = inject(ToastService);

  readonly professionalId = input.required<number>();

  protected readonly services = signal<ServiceResponse[]>([]);
  protected readonly loading = signal(true);
  protected readonly editing = signal<number | 'new' | null>(null);
  protected readonly saving = signal(false);
  protected readonly confirmDeleteId = signal<number | null>(null);

  protected readonly formatDuration = formatDuration;
  protected readonly formatPrice = formatPrice;

  ngOnInit(): void {
    this.catalog.list(this.professionalId()).subscribe({
      next: (services) => {
        this.services.set(sortServices(services));
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  protected open(target: number | 'new'): void {
    this.confirmDeleteId.set(null);
    this.editing.set(target);
  }

  protected create(request: ServiceRequest): void {
    this.saving.set(true);
    this.catalog.create(this.professionalId(), request).subscribe({
      next: (created) => {
        this.services.update((list) => sortServices([...list, created]));
        this.finish('Serviço adicionado.');
      },
      error: (err: HttpErrorResponse) => this.fail(err)
    });
  }

  protected update(service: ServiceResponse, request: ServiceRequest): void {
    this.saving.set(true);
    this.catalog.update(this.professionalId(), service.id, request).subscribe({
      next: (updated) => {
        this.replace(updated);
        this.finish('Serviço atualizado.');
      },
      error: (err: HttpErrorResponse) => this.fail(err)
    });
  }

  protected toggleActive(service: ServiceResponse): void {
    this.saving.set(true);
    this.catalog
      .update(this.professionalId(), service.id, {
        name: service.name,
        description: service.description ?? undefined,
        duration_minutes: service.duration_minutes,
        price: service.price,
        is_active: !service.is_active
      })
      .subscribe({
        next: (updated) => {
          this.replace(updated);
          this.finish(updated.is_active ? 'Serviço reativado.' : 'Serviço desativado. Ele não aparece mais para clientes.');
        },
        error: (err: HttpErrorResponse) => this.fail(err)
      });
  }

  protected remove(service: ServiceResponse): void {
    if (this.confirmDeleteId() !== service.id) {
      this.confirmDeleteId.set(service.id);
      return;
    }
    this.saving.set(true);
    this.catalog.delete(this.professionalId(), service.id).subscribe({
      next: () => {
        this.services.update((list) => list.filter((s) => s.id !== service.id));
        this.finish('Serviço excluído.');
      },
      error: (err: HttpErrorResponse) => {
        this.confirmDeleteId.set(null);
        this.fail(err);
      }
    });
  }

  private replace(updated: ServiceResponse): void {
    this.services.update((list) => sortServices(list.map((s) => (s.id === updated.id ? updated : s))));
  }

  private finish(message: string): void {
    this.saving.set(false);
    this.editing.set(null);
    this.confirmDeleteId.set(null);
    this.toast.success(message);
  }

  private fail(err: HttpErrorResponse): void {
    this.saving.set(false);
    if (err.status > 0 && err.status < 500) {
      this.toast.error(apiErrorMessage(err, 'Não foi possível salvar o serviço.'));
    }
  }
}

function sortServices(services: ServiceResponse[]): ServiceResponse[] {
  return [...services].sort(
    (a, b) => Number(b.is_active) - Number(a.is_active) || a.name.localeCompare(b.name, 'pt-BR')
  );
}
