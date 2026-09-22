import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';
import { SearchQuery, SearchResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class SearchService {
  private readonly http = inject(HttpClient);

  search(query: SearchQuery): Observable<SearchResponse> {
    let params = new HttpParams();
    if (query.city) params = params.set('city', query.city);
    if (query.state) params = params.set('state', query.state);
    if (query.service) params = params.set('service', query.service);
    if (query.page) params = params.set('page', query.page);
    if (query.size) params = params.set('size', query.size);

    return this.http.get<SearchResponse>(`${API_BASE_URL}/search`, { params });
  }
}
