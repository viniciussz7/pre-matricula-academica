import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { FirstAccessDTO, FirstAccessRequestDTO, LoginRequest, LoginResponse, AdminResponseDTO, StudentResponseDTO } from '../models/auth.model';
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/auth';
  currentUser: any = null;

  constructor(private http: HttpClient) {

  }

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((response) => {
        localStorage.setItem('token', response.token);
        localStorage.setItem('role', response.role);
        if (response.user) {
          localStorage.setItem('userData', JSON.stringify(response.user));
        }
      })
    );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('userData');
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  getUserRole(): string | null {
    return localStorage.getItem('role');
  }

  getUserData(): AdminResponseDTO | StudentResponseDTO | null {
    const userData = localStorage.getItem('userData');
    return userData ? JSON.parse(userData) : null;
  }
  
  requestFirstAccess(dto: FirstAccessRequestDTO): Observable<{ token: string }> {
    return this.http.post<{ token: string }>(`${this.apiUrl}/first-access/request`, dto);
  }

  confirmFirstAccess(dto: FirstAccessDTO): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/first-access/confirm`, dto);
  }

}