export type AppointmentStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED' | 'NO_SHOW';

export interface AuthResponse {
  token: string;
  professional_id: number | null;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  brand_name: string;
  professional_name: string;
  email: string;
  password: string;
  phone?: string;
  city: string;
  state: string;
}

export interface ServiceRequest {
  name: string;
  description?: string;
  duration_minutes: number;
  price: number;
  is_active?: boolean;
}

export interface ServiceResponse {
  id: number;
  professional_id: number;
  name: string;
  description: string | null;
  duration_minutes: number;
  price: number;
  is_active: boolean;
}

export type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

export interface ScheduleRequest {
  day_of_week: DayOfWeek;
  start_time: string;
  end_time: string;
}

export interface ScheduleResponse extends ScheduleRequest {
  id: number;
  professional_id: number;
}

export interface BlockRequest {
  start_datetime: string;
  end_datetime: string;
  reason?: string;
}

export interface BlockResponse {
  id: number;
  professional_id: number;
  start_datetime: string;
  end_datetime: string;
  reason: string | null;
}

export interface BrandResponse {
  id: number;
  name: string;
  slug: string;
  description: string | null;
  logo_url: string | null;
  city: string | null;
  state: string | null;
  phone: string | null;
  email: string | null;
  website: string | null;
}

export interface ProfessionalResponse {
  id: number;
  brand_id: number;
  name: string;
  bio: string | null;
  phone: string | null;
  email: string | null;
  photo_url: string | null;
  is_active: boolean;
}

export interface SearchQuery {
  city?: string;
  state?: string;
  service?: string;
  page?: number;
  size?: number;
}

export interface MatchedService {
  id: number;
  name: string;
  duration_minutes: number;
  price: number;
}

export interface SearchResult {
  brand_id: number;
  brand_name: string;
  slug: string;
  description: string | null;
  city: string;
  state: string;
  logo_url: string | null;
  services: MatchedService[];
}

export interface SearchResponse {
  content: SearchResult[];
  page: number;
  size: number;
  total_elements: number;
  total_pages: number;
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
