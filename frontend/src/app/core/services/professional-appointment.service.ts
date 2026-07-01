import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { AppointmentResponse, AppointmentStatus } from '../models/api.models';

export interface AppointmentFilter {
  startDate?: string;
  endDate?: string;
  status?: AppointmentStatus;
}

@Injectable({ providedIn: 'root' })
export class ProfessionalAppointmentService {
  private readonly http = inject(HttpClient);

  list(professionalId: number, filter: AppointmentFilter = {}): Observable<AppointmentResponse[]> {
    const params: Record<string, string> = {};
    if (filter.startDate) params['start_date'] = filter.startDate;
    if (filter.endDate) params['end_date'] = filter.endDate;
    if (filter.status) params['status'] = filter.status;

    return this.http.get<AppointmentResponse[]>(`${API_BASE_URL}/professionals/${professionalId}/appointments`, { params });
  }

  cancel(professionalId: number, appointmentId: number): Observable<AppointmentResponse> {
    return this.http.post<AppointmentResponse>(
      `${API_BASE_URL}/professionals/${professionalId}/appointments/${appointmentId}/cancel`,
      {}
    );
  }

  updateStatus(professionalId: number, appointmentId: number, status: AppointmentStatus): Observable<AppointmentResponse> {
    return this.http.patch<AppointmentResponse>(
      `${API_BASE_URL}/professionals/${professionalId}/appointments/${appointmentId}/status`,
      { status }
    );
  }
}
