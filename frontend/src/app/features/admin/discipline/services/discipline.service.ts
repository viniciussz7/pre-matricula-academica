import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Discipline } from '../models/discipline.model';

@Injectable({
  providedIn: 'root'
})
export class DisciplineService {
  private apiUrl = 'http://localhost:8080/disciplines';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Discipline[]> {
    return this.http.get<Discipline[]>(this.apiUrl);
  }

  create(discipline: Discipline): Observable<Discipline> {
    return this.http.post<Discipline>(this.apiUrl, discipline);
  }

  update(id: string, discipline: Discipline): Observable<Discipline> {
    return this.http.put<Discipline>(`${this.apiUrl}/${id}`, discipline);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}