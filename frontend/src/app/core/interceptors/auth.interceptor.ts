import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../../shared/feedback/toast.service';
import { SessionStore } from '../services/session-store';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionStore);
  const router = inject(Router);
  const toast = inject(ToastService);

  const isClientRoute =
    ((req.url.includes('/appointments') || req.url.includes('/waiting-list')) && !req.url.includes('/professionals/')) || req.url.endsWith('/auth/client/me') || req.url.endsWith('/auth/client/logout');
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

  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })).pipe(
    catchError((error: HttpErrorResponse) => {
      if (isProfessionalRoute && error.status === 401 && session.professionalToken === token) {
        session.clearProfessionalSession();
        toast.info('Sua sessão expirou. Entre de novo para continuar.');
        router.navigate(['/profissional/login']);
      }
      return throwError(() => error);
    })
  );
};
