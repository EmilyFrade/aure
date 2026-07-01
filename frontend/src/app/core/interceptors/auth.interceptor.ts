import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { SessionStore } from '../services/session-store';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionStore);

  const isClientRoute = req.url.includes('/appointments') && !req.url.includes('/professionals/');
  const isProfessionalRoute = req.url.includes('/professionals/') || req.url.endsWith('/auth/logout');

  let token: string | null = null;
  if (isClientRoute) {
    token = session.clientToken;
  } else if (isProfessionalRoute) {
    token = session.professionalToken;
  }

  if (!token) {
    return next(req);
  }

  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }));
};
