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
  brand_name?: string;
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

export interface ProfessionalProfile {
  name: string;
  bio: string | null;
  phone: string | null;
  photo_url: string | null;
  brand_name: string;
  brand_slug: string;
  address: string | null;
  city: string;
  state: string;
}

export interface ProfessionalProfileRequest {
  name: string;
  bio?: string;
  phone?: string;
  brand_name?: string;
  address?: string;
  city: string;
  state: string;
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
  professional_id: number;
  professional_name: string;
  bio: string | null;
  photo_url: string | null;
  brand_id: number;
  brand_name: string;
  slug: string;
  city: string;
  state: string;
  services: MatchedService[];
}

export interface SearchResponse {
  content: SearchResult[];
  page: number;
  size: number;
  total_elements: number;
  total_pages: number;
}

export interface ClientAuthResponse {
  token: string;
  name: string | null;
}

export interface ClientProfile {
  id: number;
  name: string | null;
  phone: string;
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
  phone: string | null;
  photo_url: string | null;
  services: PublicService[];
}

export interface PublicProfile {
  brand_id: number;
  brand_name: string;
  address: string | null;
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
  professional_photo_url: string | null;
  brand_name: string;
  brand_slug: string;
  client_id: number;
  client_name: string | null;
  client_phone: string;
  service_id: number;
  service_name: string;
  scheduled_date: string;
  scheduled_time: string;
  duration_minutes: number;
  price: number;
  status: AppointmentStatus;
  notes: string | null;
}

export type WaitingListStatus = 'WAITING' | 'NOTIFIED' | 'CANCELLED';

export type DayPeriod = 'MORNING' | 'AFTERNOON' | 'EVENING';

export interface WaitingListRequest {
  professional_id: number;
  service_id: number;
  preferred_date_start: string;
  preferred_date_end: string;
  preferred_period?: DayPeriod | null;
}

export interface ManualWaitingListRequest {
  service_id: number;
  client_phone: string;
  client_name?: string;
  preferred_date_start: string;
  preferred_date_end: string;
  preferred_period?: DayPeriod | null;
}

export interface WaitingListEntry {
  id: number;
  professional_id: number;
  professional_name: string;
  professional_photo_url: string | null;
  brand_name: string;
  brand_slug: string;
  client_id: number;
  client_name: string | null;
  client_phone: string;
  service_id: number;
  service_name: string;
  preferred_date_start: string;
  preferred_date_end: string;
  preferred_period: DayPeriod | null;
  position: number;
  status: WaitingListStatus;
  offered_date: string | null;
  offered_time: string | null;
  created_at: string;
}

export interface ManualAppointmentRequest {
  service_id: number;
  scheduled_date: string;
  scheduled_time: string;
  client_phone: string;
  client_name?: string;
  notes?: string;
}

export interface AppointmentStatusUpdate {
  status: AppointmentStatus;
}
