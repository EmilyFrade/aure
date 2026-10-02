import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { ClientAuthResponse, ClientOtpRequest, ClientOtpVerify, ClientProfile } from '../models/api.models';
import { SessionStore } from './session-store';

@Injectable({ providedIn: 'root' })
export class ClientAuthService {
  private readonly http = inject(HttpClient);
  private readonly session = inject(SessionStore);

  requestOtp(phone: string): Observable<void> {
    const request: ClientOtpRequest = { phone };
    return this.http.post<void>(`${API_BASE_URL}/auth/client/request-otp`, request);
  }

  verifyOtp(phone: string, code: string): Observable<ClientAuthResponse> {
    const request: ClientOtpVerify = { phone, code };
    return this.http.post<ClientAuthResponse>(`${API_BASE_URL}/auth/client/verify`, request).pipe(
      tap((response) => this.session.setClientSession(response.token))
    );
  }

  me(): Observable<ClientProfile> {
    return this.http.get<ClientProfile>(`${API_BASE_URL}/auth/client/me`);
  }

  updateName(name: string): Observable<ClientProfile> {
    return this.http.patch<ClientProfile>(`${API_BASE_URL}/auth/client/me`, { name });
  }
}
