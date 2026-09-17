import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { BlockRequest, BlockResponse, ScheduleRequest, ScheduleResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ScheduleService {
  private readonly http = inject(HttpClient);

  list(professionalId: number): Observable<ScheduleResponse[]> {
    return this.http.get<ScheduleResponse[]>(`${this.base(professionalId)}/schedules`);
  }

  create(professionalId: number, request: ScheduleRequest): Observable<ScheduleResponse> {
    return this.http.post<ScheduleResponse>(`${this.base(professionalId)}/schedules`, request);
  }

  update(professionalId: number, scheduleId: number, request: ScheduleRequest): Observable<ScheduleResponse> {
    return this.http.put<ScheduleResponse>(`${this.base(professionalId)}/schedules/${scheduleId}`, request);
  }

  delete(professionalId: number, scheduleId: number): Observable<void> {
    return this.http.delete<void>(`${this.base(professionalId)}/schedules/${scheduleId}`);
  }

  listBlocks(professionalId: number): Observable<BlockResponse[]> {
    return this.http.get<BlockResponse[]>(`${this.base(professionalId)}/blocks`);
  }

  createBlock(professionalId: number, request: BlockRequest): Observable<BlockResponse> {
    return this.http.post<BlockResponse>(`${this.base(professionalId)}/blocks`, request);
  }

  deleteBlock(professionalId: number, blockId: number): Observable<void> {
    return this.http.delete<void>(`${this.base(professionalId)}/blocks/${blockId}`);
  }

  private base(professionalId: number): string {
    return `${API_BASE_URL}/professionals/${professionalId}`;
  }
}
