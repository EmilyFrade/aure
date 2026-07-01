import { Injectable } from '@angular/core';

const PROFESSIONAL_TOKEN_KEY = 'aure_professional_token';
const PROFESSIONAL_ID_KEY = 'aure_professional_id';
const CLIENT_TOKEN_KEY = 'aure_client_token';

@Injectable({ providedIn: 'root' })
export class SessionStore {
  get professionalToken(): string | null {
    return localStorage.getItem(PROFESSIONAL_TOKEN_KEY);
  }

  get professionalId(): number | null {
    const value = localStorage.getItem(PROFESSIONAL_ID_KEY);
    return value ? Number(value) : null;
  }

  get clientToken(): string | null {
    return localStorage.getItem(CLIENT_TOKEN_KEY);
  }

  setProfessionalSession(token: string, professionalId: number): void {
    localStorage.setItem(PROFESSIONAL_TOKEN_KEY, token);
    localStorage.setItem(PROFESSIONAL_ID_KEY, String(professionalId));
  }

  clearProfessionalSession(): void {
    localStorage.removeItem(PROFESSIONAL_TOKEN_KEY);
    localStorage.removeItem(PROFESSIONAL_ID_KEY);
  }

  setClientSession(token: string): void {
    localStorage.setItem(CLIENT_TOKEN_KEY, token);
  }

  clearClientSession(): void {
    localStorage.removeItem(CLIENT_TOKEN_KEY);
  }
}
