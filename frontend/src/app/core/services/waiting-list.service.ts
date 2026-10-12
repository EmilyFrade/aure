import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { WaitingListEntry } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class WaitingListService {
  private readonly http = inject(HttpClient);

  listForProfessional(professionalId: number): Observable<WaitingListEntry[]> {
    return this.http.get<WaitingListEntry[]>(`${API_BASE_URL}/professionals/${professionalId}/waiting-list`);
  }

  remove(professionalId: number, id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/professionals/${professionalId}/waiting-list/${id}`);
  }
}
