import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AcademicPeriod } from '../models/academic-period.model';

@Injectable({ providedIn: 'root' })
export class AcademicPeriodService {
  private apiUrl = 'http://localhost:8080/academic-periods';

  constructor(private http: HttpClient) { }

  getAll(): Observable<AcademicPeriod[]> {
    return this.http.get<AcademicPeriod[]>(this.apiUrl);
  }

  getById(id: string): Observable<AcademicPeriod> {
    return this.http.get<AcademicPeriod>(`${this.apiUrl}/${id}`);
  }

  create(data: AcademicPeriod): Observable<AcademicPeriod> {
    return this.http.post<AcademicPeriod>(this.apiUrl, data);
  }

  update(id: string, data: AcademicPeriod): Observable<AcademicPeriod> {
    return this.http.put<AcademicPeriod>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}