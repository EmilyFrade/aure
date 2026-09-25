import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SearchResult } from '../../core/models/api.models';
import { SearchService } from '../../core/services/search.service';
import { PublicShell } from '../../shared/layout/public-shell';
import { Avatar } from '../../shared/ui/avatar';
import { Button } from '../../shared/ui/button';
import { Card } from '../../shared/ui/card';
import { EmptyState } from '../../shared/ui/empty-state';
import { Icon } from '../../shared/ui/icon';
import { Spinner } from '../../shared/ui/spinner';
import { TextField } from '../../shared/ui/text-field';
import { BR_UFS } from '../../shared/utils/br-states';
import { formatDuration, formatPrice } from '../../shared/utils/format';

const PAGE_SIZE = 10;

@Component({
  selector: 'app-search-page',
  imports: [FormsModule, RouterLink, PublicShell, Avatar, Button, Card, EmptyState, Icon, Spinner, TextField],
  templateUrl: './search-page.html'
})
export class SearchPage {
  private readonly searchService = inject(SearchService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly city = signal(this.route.snapshot.queryParamMap.get('cidade') ?? '');
  protected readonly state = signal(this.route.snapshot.queryParamMap.get('uf') ?? '');
  protected readonly service = signal(this.route.snapshot.queryParamMap.get('servico') ?? '');

  protected readonly results = signal<SearchResult[]>([]);
  protected readonly total = signal(0);
  protected readonly page = signal(0);
  protected readonly totalPages = signal(0);
  protected readonly loading = signal(true);
  protected readonly loadingMore = signal(false);
  protected readonly failed = signal(false);

  protected readonly ufs = BR_UFS;
  protected readonly formatPrice = formatPrice;
  protected readonly formatDuration = formatDuration;

  protected readonly hasMore = computed(() => this.page() + 1 < this.totalPages());

  constructor() {
    this.load(0);
  }

  protected submit(): void {
    this.router.navigate([], {
      queryParams: {
        cidade: this.city().trim() || null,
        uf: this.state() || null,
        servico: this.service().trim() || null
      },
      replaceUrl: true
    });
    this.load(0);
  }

  protected loadMore(): void {
    if (!this.hasMore() || this.loadingMore()) return;
    this.load(this.page() + 1);
  }

  private load(page: number): void {
    const appending = page > 0;
    if (appending) this.loadingMore.set(true);
    else this.loading.set(true);
    this.failed.set(false);

    this.searchService
      .search({
        city: this.city().trim(),
        state: this.state(),
        service: this.service().trim(),
        page,
        size: PAGE_SIZE
      })
      .subscribe({
        next: (response) => {
          this.results.update((current) => (appending ? [...current, ...response.content] : response.content));
          this.total.set(response.total_elements);
          this.page.set(response.page);
          this.totalPages.set(response.total_pages);
          this.loading.set(false);
          this.loadingMore.set(false);
        },
        error: () => {
          this.loading.set(false);
          this.loadingMore.set(false);
          this.failed.set(true);
        }
      });
  }
}
