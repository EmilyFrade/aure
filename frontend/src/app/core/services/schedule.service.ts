import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { ScheduleRequest, ScheduleResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ScheduleService {
  private readonly http = inject(HttpClient);

  list(professionalId: number): Observable<ScheduleResponse[]> {
    return this.http.get<ScheduleResponse[]>(`${API_BASE_URL}/professionals/${professionalId}/schedules`);
  }

  create(professionalId: number, request: ScheduleRequest): Observable<ScheduleResponse> {
    return this.http.post<ScheduleResponse>(`${API_BASE_URL}/professionals/${professionalId}/schedules`, request);
  }
}
