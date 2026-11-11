import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { MonthlyReport } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private readonly http = inject(HttpClient);

  monthly(professionalId: number, month: string): Observable<MonthlyReport> {
    return this.http.get<MonthlyReport>(`${API_BASE_URL}/professionals/${professionalId}/report`, { params: { month } });
  }
}
