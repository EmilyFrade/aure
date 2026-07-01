export type AppointmentStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED' | 'NO_SHOW';

export interface AuthResponse {
  token: string;
  professional_id: number | null;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface ClientOtpRequest {
  phone: string;
}

export interface ClientOtpVerify {
  phone: string;
  code: string;
}

export interface PublicService {
  id: number;
  name: string;
  description: string | null;
  duration_minutes: number;
  price: number;
}

export interface PublicProfessional {
  id: number;
  name: string;
  bio: string | null;
  photo_url: string | null;
  services: PublicService[];
}

export interface PublicProfile {
  brand_id: number;
  brand_name: string;
  description: string | null;
  city: string | null;
  state: string | null;
  professionals: PublicProfessional[];
}

export interface AvailabilityResponse {
  date: string;
  slots: string[];
}

export interface AppointmentRequest {
  professional_id: number;
  service_id: number;
  scheduled_date: string;
  scheduled_time: string;
  notes?: string;
}

export interface AppointmentResponse {
  id: number;
  professional_id: number;
  professional_name: string;
  client_id: number;
  client_name: string;
  service_id: number;
  service_name: string;
  scheduled_date: string;
  scheduled_time: string;
  duration_minutes: number;
  price: number;
  status: AppointmentStatus;
  notes: string | null;
}

export interface AppointmentStatusUpdate {
  status: AppointmentStatus;
}
