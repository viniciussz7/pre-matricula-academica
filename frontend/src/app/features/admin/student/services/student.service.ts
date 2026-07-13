import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Student } from '../models/student.model';

@Injectable({ providedIn: 'root' })
export class StudentService {
  private apiUrl = 'http://localhost:8080/students';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Student[]> { return this.http.get<Student[]>(this.apiUrl); }
  create(data: Student): Observable<Student> { return this.http.post<Student>(this.apiUrl, data); }
  update(id: string, data: Student): Observable<Student> { return this.http.put<Student>(`${this.apiUrl}/${id}`, data); }
  delete(id: string): Observable<void> { return this.http.delete<void>(`${this.apiUrl}/${id}`); }
}