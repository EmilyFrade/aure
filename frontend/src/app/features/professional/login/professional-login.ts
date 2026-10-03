import { HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ProfessionalAuthService } from '../../../core/services/professional-auth.service';
import { PublicShell } from '../../../shared/layout/public-shell';
import { Button } from '../../../shared/ui/button';
import { Card } from '../../../shared/ui/card';
import { Icon } from '../../../shared/ui/icon';
import { TextField } from '../../../shared/ui/text-field';

@Component({
  selector: 'app-professional-login',
  imports: [FormsModule, RouterLink, PublicShell, Button, Card, Icon, TextField],
  templateUrl: './professional-login.html'
})
export class ProfessionalLogin {
  private readonly authService = inject(ProfessionalAuthService);
  private readonly router = inject(Router);

  protected readonly email = signal('');
  protected readonly password = signal('');
  protected readonly showPassword = signal(false);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly canSubmit = computed(() => !!this.email().trim() && !!this.password());

  constructor() {
    if (this.authService.isAuthenticated) {
      this.router.navigate(['/profissional/agenda'], { replaceUrl: true });
    }
  }

  protected submit(): void {
    if (!this.canSubmit() || this.submitting()) return;

    this.submitting.set(true);
    this.errorMessage.set(null);
    this.authService.login({ email: this.email().trim(), password: this.password() }).subscribe({
      next: () => {
        this.submitting.set(false);
        this.router.navigate(['/profissional/agenda']);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        if (err.status > 0 && err.status < 500) {
          this.errorMessage.set('E-mail ou senha incorretos.');
        }
      }
    });
  }
}
