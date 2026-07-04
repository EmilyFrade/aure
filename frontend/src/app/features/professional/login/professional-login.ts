import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProfessionalAuthService } from '../../../core/services/professional-auth.service';

@Component({
  selector: 'app-professional-login',
  imports: [FormsModule],
  templateUrl: './professional-login.html'
})
export class ProfessionalLogin {
  private readonly authService = inject(ProfessionalAuthService);
  private readonly router = inject(Router);

  readonly email = signal('');
  readonly password = signal('');
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  submit(): void {
    if (!this.email().trim() || !this.password().trim()) return;

    this.submitting.set(true);
    this.errorMessage.set(null);
    this.authService.login({ email: this.email().trim(), password: this.password() }).subscribe({
      next: () => {
        this.submitting.set(false);
        this.router.navigate(['/profissional/agenda']);
      },
      error: () => {
        this.submitting.set(false);
        this.errorMessage.set('E-mail ou senha inválidos.');
      }
    });
  }
}
