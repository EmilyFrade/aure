import { Routes } from '@angular/router';
import { professionalGuard } from './core/guards/professional.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/home/home').then((m) => m.Home)
  },
  {
    path: 'buscar',
    loadComponent: () => import('./features/search/search-page').then((m) => m.SearchPage)
  },
  {
    path: 'agendar/:slug',
    loadComponent: () => import('./features/booking/booking-page').then((m) => m.BookingPage)
  },
  {
    path: 'profissional/cadastro',
    loadComponent: () =>
      import('./features/professional/onboarding/professional-onboarding').then((m) => m.ProfessionalOnboarding)
  },
  {
    path: 'profissional/primeiros-passos',
    loadComponent: () =>
      import('./features/professional/setup/professional-setup').then((m) => m.ProfessionalSetup),
    canActivate: [professionalGuard]
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
  {
    path: 'profissional/configuracoes',
    loadComponent: () =>
      import('./features/professional/settings/professional-settings').then((m) => m.ProfessionalSettings),
    canActivate: [professionalGuard]
  },
  { path: '**', redirectTo: '' }
];
