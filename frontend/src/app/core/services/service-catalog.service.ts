import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { ServiceRequest, ServiceResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ServiceCatalogService {
  private readonly http = inject(HttpClient);

  list(professionalId: number): Observable<ServiceResponse[]> {
    return this.http.get<ServiceResponse[]>(this.url(professionalId));
  }

  create(professionalId: number, request: ServiceRequest): Observable<ServiceResponse> {
    return this.http.post<ServiceResponse>(this.url(professionalId), request);
  }

  update(professionalId: number, serviceId: number, request: ServiceRequest): Observable<ServiceResponse> {
    return this.http.put<ServiceResponse>(`${this.url(professionalId)}/${serviceId}`, request);
  }

  delete(professionalId: number, serviceId: number): Observable<void> {
    return this.http.delete<void>(`${this.url(professionalId)}/${serviceId}`);
  }

  private url(professionalId: number): string {
    return `${API_BASE_URL}/professionals/${professionalId}/services`;
  }
}
