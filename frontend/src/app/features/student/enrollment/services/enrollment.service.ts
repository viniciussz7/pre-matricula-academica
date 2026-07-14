import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Enrollment } from '../models/enrollment.model';

@Injectable({ providedIn: 'root' })
export class EnrollmentService {
  private apiUrl = 'http://localhost:8080/enrollments';

  constructor(private http: HttpClient) { }

  create(dto: any): Observable<Enrollment> {
    return this.http.post<Enrollment>(this.apiUrl, dto);
  }

  update(enrollmentId: string, dto: any): Observable<Enrollment> {
    return this.http.put<Enrollment>(`${this.apiUrl}/${enrollmentId}`, dto);
  }

  cancel(enrollmentId: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${enrollmentId}/cancel`, {});
  }

  findMyEnrollment(): Observable<Enrollment> {
    return this.http.get<Enrollment>(`${this.apiUrl}/me`);
  }

  findById(id: string): Observable<Enrollment> {
    return this.http.get<Enrollment>(`${this.apiUrl}/${id}`);
  }

  findByProcess(processId: string): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.apiUrl}/process/${processId}`);
  }

  findMyHistory(): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.apiUrl}/me/history`);
  }

  findByStudent(studentId: string): Observable<Enrollment[]> {
    return this.http.get<Enrollment[]>(`${this.apiUrl}/student/${studentId}`);
  }

}