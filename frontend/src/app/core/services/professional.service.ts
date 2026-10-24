import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { ProfessionalProfile, ProfessionalProfileRequest, ProfessionalResponse } from '../models/api.models';

export interface ProfessionalIdentity {
  name: string;
  photoUrl: string | null;
}

@Injectable({ providedIn: 'root' })
export class ProfessionalService {
  private readonly http = inject(HttpClient);
  private readonly identityState = signal<ProfessionalIdentity | null>(null);

  readonly identity = this.identityState.asReadonly();

  get(professionalId: number): Observable<ProfessionalResponse> {
    return this.http.get<ProfessionalResponse>(`${API_BASE_URL}/professionals/${professionalId}`);
  }

  loadIdentity(professionalId: number): void {
    this.get(professionalId).subscribe((p) => this.identityState.set({ name: p.name, photoUrl: p.photo_url }));
  }

  getProfile(professionalId: number): Observable<ProfessionalProfile> {
    return this.http.get<ProfessionalProfile>(this.url(professionalId, 'profile'));
  }

  updateProfile(professionalId: number, request: ProfessionalProfileRequest): Observable<ProfessionalProfile> {
    return this.http
      .put<ProfessionalProfile>(this.url(professionalId, 'profile'), request)
      .pipe(tap((profile) => this.updateIdentity(profile)));
  }

  uploadPhoto(professionalId: number, photo: Blob): Observable<ProfessionalProfile> {
    const body = new FormData();
    body.append('file', photo, 'foto.jpg');
    return this.http
      .put<ProfessionalProfile>(this.url(professionalId, 'photo'), body)
      .pipe(tap((profile) => this.updateIdentity(profile)));
  }

  deletePhoto(professionalId: number): Observable<ProfessionalProfile> {
    return this.http
      .delete<ProfessionalProfile>(this.url(professionalId, 'photo'))
      .pipe(tap((profile) => this.updateIdentity(profile)));
  }

  private updateIdentity(profile: ProfessionalProfile): void {
    this.identityState.set({ name: profile.name, photoUrl: profile.photo_url });
  }

  private url(professionalId: number, path: string): string {
    return `${API_BASE_URL}/professionals/${professionalId}/${path}`;
  }
}
