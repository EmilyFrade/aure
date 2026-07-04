import { Routes } from '@angular/router';
import { professionalGuard } from './core/guards/professional.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/home/home').then((m) => m.Home)
  },
  {
    path: 'agendar/:slug',
    loadComponent: () => import('./features/booking/booking-page').then((m) => m.BookingPage)
  },
  {
    path: 'profissional/login',
    loadComponent: () => import('./features/professional/login/professional-login').then((m) => m.ProfessionalLogin)
  },
  {
    path: 'profissional/agenda',
    loadComponent: () => import('./features/professional/agenda/professional-agenda').then((m) => m.ProfessionalAgenda),
    canActivate: [professionalGuard]
  },
  { path: '**', redirectTo: '' }
];
