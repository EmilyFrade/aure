import { Component, computed, input, model, signal } from '@angular/core';

@Component({
  selector: 'app-otp-input',
  template: `
    <div class="relative">
      <div class="flex justify-center gap-2" aria-hidden="true">
        @for (i of indexes(); track i) {
          <div
            class="flex h-12 w-11 items-center justify-center rounded-xl border bg-white text-lg font-semibold text-slate-800 transition sm:h-14 sm:w-12"
            [class]="
              focused() && i === activeIndex()
                ? 'border-primary-500 ring-2 ring-primary-100'
                : value()[i]
                  ? 'border-primary-200'
                  : 'border-slate-200'
            ">
            {{ value()[i] ?? '' }}
          </div>
        }
      </div>
      <input
        #field
        class="absolute inset-0 h-full w-full cursor-text opacity-0"
        [attr.name]="name()"
        [attr.aria-label]="label()"
        [attr.maxlength]="length()"
        [value]="value()"
        inputmode="numeric"
        autocomplete="one-time-code"
        (input)="onInput(field)"
        (focus)="focused.set(true)"
        (blur)="focused.set(false)" />
    </div>
  `,
  styles: `:host { display: block; }`
})
export class OtpInput {
  readonly value = model('');
  readonly length = input(6);
  readonly name = input('otp');
  readonly label = input('Código de verificação');

  protected readonly focused = signal(false);
  protected readonly indexes = computed(() => Array.from({ length: this.length() }, (_, i) => i));
  protected readonly activeIndex = computed(() => Math.min(this.value().length, this.length() - 1));

  protected onInput(field: HTMLInputElement): void {
    const digits = field.value.replace(/\D/g, '').slice(0, this.length());
    field.value = digits;
    this.value.set(digits);
  }
}
