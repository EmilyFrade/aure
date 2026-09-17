import { Component, computed, inject, input } from '@angular/core';
import { ToastService } from '../../../shared/feedback/toast.service';
import { Icon } from '../../../shared/ui/icon';

@Component({
  selector: 'app-public-link-card',
  imports: [Icon],
  template: `
    <div class="flex items-center gap-2 rounded-xl border border-primary-100 bg-white py-1.5 pl-3 pr-1.5">
      <span class="min-w-0 flex-1 truncate text-sm text-primary-700">{{ displayUrl() }}</span>
      <div class="flex shrink-0 items-center gap-0.5">
        <button type="button" (click)="copy()" [class]="iconClass" aria-label="Copiar link público">
          <app-icon name="copy" [size]="18" />
        </button>
        <a [href]="whatsappUrl()" target="_blank" rel="noopener" [class]="iconClass" aria-label="Compartilhar no WhatsApp">
          <app-icon name="share" [size]="18" />
        </a>
      </div>
    </div>
  `,
  styles: `:host { display: block; }`
})
export class PublicLinkCard {
  private readonly toast = inject(ToastService);

  readonly slug = input.required<string>();
  readonly brandName = input('');

  protected readonly iconClass =
    'flex h-9 w-9 shrink-0 items-center justify-center rounded-lg text-slate-500 transition hover:bg-primary-50 hover:text-primary-700';

  protected readonly url = computed(() => `${location.origin}/agendar/${this.slug()}`);
  protected readonly displayUrl = computed(() => this.url().replace(/^https?:\/\//, ''));

  protected readonly whatsappUrl = computed(() => {
    const intro = this.brandName() ? `Agora você pode agendar comigo no ${this.brandName()} pelo link:` : 'Agora você pode agendar comigo pelo link:';
    return `https://wa.me/?text=${encodeURIComponent(`${intro} ${this.url()}`)}`;
  });

  protected async copy(): Promise<void> {
    try {
      if (navigator.clipboard) await navigator.clipboard.writeText(this.url());
      else this.copyWithTextarea();
      this.toast.success('Link copiado!');
    } catch {
      this.toast.error('Não foi possível copiar o link.');
    }
  }

  private copyWithTextarea(): void {
    const textarea = document.createElement('textarea');
    textarea.value = this.url();
    textarea.style.position = 'fixed';
    textarea.style.opacity = '0';
    document.body.appendChild(textarea);
    textarea.select();
    const ok = document.execCommand('copy');
    textarea.remove();
    if (!ok) throw new Error('copy failed');
  }
}
