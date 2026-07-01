import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { ProfessionalAuthService } from '../services/professional-auth.service';

export const professionalGuard: CanActivateFn = () => {
  const auth = inject(ProfessionalAuthService);
  const router = inject(Router);

  if (auth.isAuthenticated) {
    return true;
  }

  return router.createUrlTree(['/profissional/login']);
};
