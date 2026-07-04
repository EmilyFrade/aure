import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/home/home').then((m) => m.Home)
  },
  {
    path: 'agendar/:slug',
    loadComponent: () => import('./features/booking/booking-page').then((m) => m.BookingPage)
  },
  { path: '**', redirectTo: '' }
];
