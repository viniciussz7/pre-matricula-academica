import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { EnrollmentProcessClass } from '../models/enrollment-process-class.model';

export interface CreateEnrollmentProcessClassRequest {
  enrollmentProcessId: string;
  classGroupId: string;
  active?: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class EnrollmentProcessClassService {
  private readonly apiUrl =
    'http://localhost:8080/enrollment-process-classes';

  constructor(
    private readonly http: HttpClient,
  ) {}

  create(
    dto: CreateEnrollmentProcessClassRequest,
  ): Observable<EnrollmentProcessClass> {
    return this.http.post<EnrollmentProcessClass>(
      this.apiUrl,
      dto,
    );
  }

  findByProcess(
    processId: string,
  ): Observable<EnrollmentProcessClass[]> {
    return this.http.get<EnrollmentProcessClass[]>(
      `${this.apiUrl}/process/${processId}`,
    );
  }

  deactivate(id: string): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${id}`,
    );
  }

  findOpenProcessClasses():
    Observable<EnrollmentProcessClass[]> {
    return this.http.get<EnrollmentProcessClass[]>(
      `${this.apiUrl}/open`,
    );
  }
}