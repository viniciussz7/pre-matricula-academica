import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EnrollmentProcess } from '../models/enrollment-process.model';

@Injectable({ providedIn: 'root' })
export class EnrollmentProcessService {
  private apiUrl = 'http://localhost:8080/enrollment-processes';

  constructor(private http: HttpClient) { }


  create(dto: EnrollmentProcess): Observable<EnrollmentProcess> {
    return this.http.post<EnrollmentProcess>(this.apiUrl, dto);
  }

  findAll(): Observable<EnrollmentProcess[]> {
    return this.http.get<EnrollmentProcess[]>(this.apiUrl);
  }

  findById(id: string): Observable<EnrollmentProcess> {
    return this.http.get<EnrollmentProcess>(`${this.apiUrl}/${id}`);
  }

  update(id: string, dto: EnrollmentProcess): Observable<EnrollmentProcess> {
    return this.http.put<EnrollmentProcess>(`${this.apiUrl}/${id}`, dto);
  }

  deactivate(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  findOpenProcess(): Observable<EnrollmentProcess> {
    return this.http.get<EnrollmentProcess>(`${this.apiUrl}/open`);
  }
}