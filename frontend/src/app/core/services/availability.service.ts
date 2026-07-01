import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { AvailabilityResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class AvailabilityService {
  private readonly http = inject(HttpClient);

  getAvailableSlots(professionalId: number, date: string, serviceId: number): Observable<AvailabilityResponse> {
    return this.http.get<AvailabilityResponse>(`${API_BASE_URL}/professionals/${professionalId}/availability`, {
      params: { date, service_id: serviceId }
    });
  }
}
