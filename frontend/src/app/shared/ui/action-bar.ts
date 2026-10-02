import { Component } from '@angular/core';

@Component({
  selector: 'app-action-bar',
  template: `
    <div
      class="fixed inset-x-0 bottom-0 z-30 border-t border-primary-100 bg-white/95 px-4 pt-3 pb-[max(env(safe-area-inset-bottom),1rem)] backdrop-blur sm:static sm:z-auto sm:mt-5 sm:border-0 sm:bg-transparent sm:p-0 sm:backdrop-blur-none">
      <ng-content />
    </div>
  `,
  styles: `:host { display: block; }`
})
export class ActionBar {}
