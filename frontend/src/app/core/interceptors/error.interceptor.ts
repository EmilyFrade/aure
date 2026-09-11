import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from '../../shared/feedback/toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(ToastService);

  return next(req).pipe(
    catchError((error) => {
      if (error.status === 0) {
        toast.error('Sem conexão com o servidor. Verifique sua internet.');
      } else if (error.status >= 500) {
        toast.error('Ocorreu um erro no servidor. Tente novamente em instantes.');
      }
      return throwError(() => error);
    })
  );
};
