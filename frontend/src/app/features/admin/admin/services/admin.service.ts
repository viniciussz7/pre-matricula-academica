import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Admin } from '../models/admin.model';

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  private readonly apiUrl =
    'http://localhost:8080/admins';

  constructor(
    private readonly http: HttpClient,
  ) {}

  getAll(): Observable<Admin[]> {
    return this.http.get<Admin[]>(
      this.apiUrl,
    );
  }

  getById(
    id: string,
  ): Observable<Admin> {
    return this.http.get<Admin>(
      `${this.apiUrl}/${id}`,
    );
  }

  create(
    data: Admin,
  ): Observable<Admin> {
    return this.http.post<Admin>(
      this.apiUrl,
      data,
    );
  }

  update(
    id: string,
    data: Admin,
  ): Observable<Admin> {
    return this.http.put<Admin>(
      `${this.apiUrl}/${id}`,
      data,
    );
  }

  delete(
    id: string,
  ): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${id}`,
    );
  }
}