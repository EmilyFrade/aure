import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { AuthResponse, LoginRequest } from '../models/api.models';
import { SessionStore } from './session-store';

@Injectable({ providedIn: 'root' })
export class ProfessionalAuthService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionStore);

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${API_BASE_URL}/auth/login`, request).pipe(
      tap((response) => this.session.setProfessionalSession(response.token, response.professional_id!))
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${API_BASE_URL}/auth/logout`, {}).pipe(
      tap(() => this.session.clearProfessionalSession())
    );
  }

  get isAuthenticated(): boolean {
    return !!this.session.professionalToken && this.session.professionalId !== null;
  }

  get professionalId(): number | null {
    return this.session.professionalId;
  }
}
