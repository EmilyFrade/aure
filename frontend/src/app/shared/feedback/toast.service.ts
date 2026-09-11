import { Injectable, signal } from '@angular/core';

export type ToastKind = 'success' | 'error' | 'info';

export interface Toast {
  readonly id: number;
  readonly kind: ToastKind;
  readonly text: string;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  readonly toasts = signal<readonly Toast[]>([]);
  private seq = 0;

  success(text: string, durationMs = 4000): void {
    this.show(text, 'success', durationMs);
  }

  error(text: string, durationMs = 5000): void {
    this.show(text, 'error', durationMs);
  }

  info(text: string, durationMs = 4000): void {
    this.show(text, 'info', durationMs);
  }

  dismiss(id: number): void {
    this.toasts.update((list) => list.filter((t) => t.id !== id));
  }

  private show(text: string, kind: ToastKind, durationMs: number): void {
    const id = ++this.seq;
    this.toasts.update((list) => [...list, { id, kind, text }]);
    if (durationMs > 0) {
      setTimeout(() => this.dismiss(id), durationMs);
    }
  }
}
