import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { ProfessionalResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ProfessionalService {
  private readonly http = inject(HttpClient);

  get(professionalId: number): Observable<ProfessionalResponse> {
    return this.http.get<ProfessionalResponse>(`${API_BASE_URL}/professionals/${professionalId}`);
  }
}
