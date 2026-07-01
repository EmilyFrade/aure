import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { PublicProfile } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class PublicProfileService {
  private readonly http = inject(HttpClient);

  getBySlug(slug: string): Observable<PublicProfile> {
    return this.http.get<PublicProfile>(`${API_BASE_URL}/p/${slug}`);
  }
}
