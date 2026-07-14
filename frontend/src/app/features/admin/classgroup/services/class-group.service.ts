import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClassGroup } from '../models/class-group.model';

@Injectable({ providedIn: 'root' })
export class ClassGroupService {
  private apiUrl = 'http://localhost:8080/class-groups';

  constructor(private http: HttpClient) { }

  getAll(): Observable<ClassGroup[]> {
    return this.http.get<ClassGroup[]>(this.apiUrl);
  }

  getById(id: string): Observable<ClassGroup> {
      return this.http.get<ClassGroup>(`${this.apiUrl}/${id}`);
    }

  create(data: ClassGroup): Observable<ClassGroup> {
    return this.http.post<ClassGroup>(this.apiUrl, data);
  }

  update(id: string, data: ClassGroup): Observable<ClassGroup> {
    return this.http.put<ClassGroup>(`${this.apiUrl}/${id}`, data);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}