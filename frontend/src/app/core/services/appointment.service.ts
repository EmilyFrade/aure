import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { AppointmentRequest, AppointmentResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class AppointmentService {
  private readonly http = inject(HttpClient);

  create(request: AppointmentRequest): Observable<AppointmentResponse> {
    return this.http.post<AppointmentResponse>(`${API_BASE_URL}/appointments`, request);
  }

  listMine(): Observable<AppointmentResponse[]> {
    return this.http.get<AppointmentResponse[]>(`${API_BASE_URL}/appointments`);
  }

  cancel(appointmentId: number): Observable<AppointmentResponse> {
    return this.http.post<AppointmentResponse>(`${API_BASE_URL}/appointments/${appointmentId}/cancel`, {});
  }
}
