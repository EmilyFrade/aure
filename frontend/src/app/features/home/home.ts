import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ProfessionalAuthService } from '../../core/services/professional-auth.service';
import { PublicShell } from '../../shared/layout/public-shell';
import { Button } from '../../shared/ui/button';
import { Card } from '../../shared/ui/card';
import { Icon, IconName } from '../../shared/ui/icon';

interface Highlight {
  readonly icon: IconName;
  readonly title: string;
  readonly description: string;
}

@Component({
  selector: 'app-home',
  imports: [FormsModule, RouterLink, PublicShell, Button, Card, Icon],
  templateUrl: './home.html'
})
export class Home {
  private readonly router = inject(Router);

  protected readonly loggedIn = inject(ProfessionalAuthService).isAuthenticated;
  protected readonly city = signal('');
  protected readonly service = signal('');

  protected readonly highlights: readonly Highlight[] = [
    { icon: 'share', title: 'Seu link de agendamento', description: 'Clientes marcam sozinhas, a qualquer hora.' },
    { icon: 'calendar-week', title: 'Agenda no celular', description: 'Dia e semana, com confirmação e conclusão em um toque.' },
    { icon: 'lock', title: 'Horários do seu jeito', description: 'Intervalos, folgas e bloqueios sem conflito de horário.' }
  ];

  protected search(): void {
    this.router.navigate(['/buscar'], {
      queryParams: {
        cidade: this.city().trim() || null,
        servico: this.service().trim() || null
      }
    });
  }
}
