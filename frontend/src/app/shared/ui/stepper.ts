import { Component, input } from '@angular/core';
import { Icon } from './icon';

@Component({
  selector: 'app-stepper',
  imports: [Icon],
  template: `
    <ol class="flex items-center gap-2" aria-label="Etapas">
      @for (step of steps(); track step; let i = $index; let last = $last) {
        <li class="flex items-center gap-2" [class.flex-1]="!last" [attr.aria-current]="i === current() ? 'step' : null">
          <span
            class="flex h-7 w-7 shrink-0 items-center justify-center rounded-full text-xs font-semibold transition"
            [class]="
              i < current()
                ? 'bg-primary-500 text-white'
                : i === current()
                  ? 'bg-white text-primary-600 ring-2 ring-primary-500'
                  : 'bg-white text-slate-400 ring-1 ring-slate-200'
            ">
            @if (i < current()) {
              <app-icon name="check" [size]="14" />
            } @else {
              {{ i + 1 }}
            }
          </span>
          <span
            class="whitespace-nowrap text-sm"
            [class]="i === current() ? 'font-medium text-slate-800' : 'hidden text-slate-500 sm:inline'">
            {{ step }}
          </span>
          @if (!last) {
            <span class="h-px min-w-4 flex-1" [class]="i < current() ? 'bg-primary-300' : 'bg-slate-200'"></span>
          }
        </li>
      }
    </ol>
  `,
  styles: `:host { display: block; }`
})
export class Stepper {
  readonly steps = input.required<string[]>();
  readonly current = input(0);
}
