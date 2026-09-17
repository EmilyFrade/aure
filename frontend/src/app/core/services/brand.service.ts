import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, switchMap } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { BrandResponse } from '../models/api.models';
import { ProfessionalService } from './professional.service';

@Injectable({ providedIn: 'root' })
export class BrandService {
  private readonly http = inject(HttpClient);
  private readonly professionals = inject(ProfessionalService);

  get(brandId: number): Observable<BrandResponse> {
    return this.http.get<BrandResponse>(`${API_BASE_URL}/brands/${brandId}`);
  }

  forProfessional(professionalId: number): Observable<BrandResponse> {
    return this.professionals.get(professionalId).pipe(switchMap((p) => this.get(p.brand_id)));
  }
}
