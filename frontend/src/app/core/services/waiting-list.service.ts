import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { ManualWaitingListRequest, WaitingListEntry, WaitingListRequest } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class WaitingListService {
  private readonly http = inject(HttpClient);

  join(request: WaitingListRequest): Observable<WaitingListEntry> {
    return this.http.post<WaitingListEntry>(`${API_BASE_URL}/waiting-list`, request);
  }

  listMine(): Observable<WaitingListEntry[]> {
    return this.http.get<WaitingListEntry[]>(`${API_BASE_URL}/waiting-list`);
  }

  leave(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/waiting-list/${id}`);
  }

  listForProfessional(professionalId: number): Observable<WaitingListEntry[]> {
    return this.http.get<WaitingListEntry[]>(`${API_BASE_URL}/professionals/${professionalId}/waiting-list`);
  }

  add(professionalId: number, request: ManualWaitingListRequest): Observable<WaitingListEntry> {
    return this.http.post<WaitingListEntry>(`${API_BASE_URL}/professionals/${professionalId}/waiting-list`, request);
  }

  remove(professionalId: number, id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/professionals/${professionalId}/waiting-list/${id}`);
  }
}
