import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { ProfessionalAuthService } from '../../../core/services/professional-auth.service';
import { ToastService } from '../../../shared/feedback/toast.service';
import { PublicShell } from '../../../shared/layout/public-shell';
import { Card } from '../../../shared/ui/card';
import { Button } from '../../../shared/ui/button';
import { Icon } from '../../../shared/ui/icon';
import { TextField } from '../../../shared/ui/text-field';
import { maskPhoneBR, stripSpaces } from '../../../shared/utils/masks';
import { BR_UFS } from '../../../shared/utils/br-states';

@Component({
  selector: 'app-professional-onboarding',
  imports: [FormsModule, RouterLink, PublicShell, Card, Button, Icon, TextField],
  templateUrl: './professional-onboarding.html'
})
export class ProfessionalOnboarding {
  private readonly authService = inject(ProfessionalAuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly brandName = signal('');
  readonly professionalName = signal('');
  readonly phone = signal('');
  readonly city = signal('');
  readonly state = signal('');
  readonly email = signal('');
  readonly password = signal('');
  readonly confirmPassword = signal('');

  readonly submitting = signal(false);
  readonly emailTaken = signal(false);

  protected readonly maskPhone = maskPhoneBR;
  protected readonly stripSpaces = stripSpaces;
  protected readonly ufs = BR_UFS;

  private readonly emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  readonly emailError = computed(() => {
    if (this.emailTaken()) return 'Este e-mail já está cadastrado.';
    const email = this.email().trim();
    if (email && !this.emailPattern.test(email)) return 'Informe um e-mail válido.';
    return null;
  });

  readonly passwordError = computed(() => {
    const pass = this.password();
    if (pass && pass.length < 6) return 'A senha deve ter ao menos 6 caracteres.';
    if (this.confirmPassword() && this.confirmPassword() !== pass) return 'As senhas não coincidem.';
    return null;
  });

  readonly canSubmit = computed(
    () =>
      !!this.brandName().trim() &&
      !!this.professionalName().trim() &&
      !!this.city().trim() &&
      !!this.state() &&
      this.emailPattern.test(this.email().trim()) &&
      this.password().length >= 6 &&
      this.confirmPassword() === this.password()
  );

  submit(): void {
    if (!this.canSubmit() || this.submitting()) return;

    this.submitting.set(true);
    this.authService
      .signup({
        brand_name: this.brandName().trim(),
        professional_name: this.professionalName().trim(),
        email: this.email().trim(),
        password: this.password(),
        phone: this.phone().trim() || undefined,
        city: this.city().trim(),
        state: this.state().trim().toUpperCase()
      })
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.toast.success('Conta criada! Bem-vinda à Aure.');
          this.router.navigate(['/profissional/primeiros-passos']);
        },
        error: (err: HttpErrorResponse) => {
          this.submitting.set(false);
          if (err.status === 409) {
            this.emailTaken.set(true);
          } else if (err.status === 400) {
            this.toast.error('Confira os dados informados e tente novamente.');
          }
        }
      });
  }
}
