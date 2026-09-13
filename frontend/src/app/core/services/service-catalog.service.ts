import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { ServiceRequest, ServiceResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ServiceCatalogService {
  private readonly http = inject(HttpClient);

  list(professionalId: number): Observable<ServiceResponse[]> {
    return this.http.get<ServiceResponse[]>(`${API_BASE_URL}/professionals/${professionalId}/services`);
  }

  create(professionalId: number, request: ServiceRequest): Observable<ServiceResponse> {
    return this.http.post<ServiceResponse>(`${API_BASE_URL}/professionals/${professionalId}/services`, request);
  }
}
