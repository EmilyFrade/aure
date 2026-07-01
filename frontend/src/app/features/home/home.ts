import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  imports: [FormsModule, RouterLink],
  templateUrl: './home.html'
})
export class Home {
  private readonly router = inject(Router);

  readonly slug = signal('');

  goToBooking(): void {
    const value = this.slug().trim();
    if (!value) return;
    this.router.navigate(['/agendar', value]);
  }
}
