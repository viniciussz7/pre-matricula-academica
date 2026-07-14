import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  ClassDemand,
  EnrolledStudent,
  ProcessSummary,
  StudentWithoutEnrollment,
} from '../models/report.model';

@Injectable({
  providedIn: 'root',
})
export class ReportService {
  private readonly apiUrl =
    'http://localhost:8080/reports';

  constructor(
    private readonly http: HttpClient,
  ) {}

  findProcessSummary(
    processId: string,
  ): Observable<ProcessSummary> {
    return this.http.get<ProcessSummary>(
      `${this.apiUrl}/processes/${processId}/summary`,
    );
  }

  findClassDemand(
    processId: string,
  ): Observable<ClassDemand[]> {
    return this.http.get<ClassDemand[]>(
      `${this.apiUrl}/processes/${processId}/class-demand`,
    );
  }

  findStudentsByProcessClass(
    processClassId: string,
  ): Observable<EnrolledStudent[]> {
    return this.http.get<EnrolledStudent[]>(
      `${this.apiUrl}/process-classes/${processClassId}/students`,
    );
  }

  findStudentsWithoutEnrollment(
    processId: string,
  ): Observable<StudentWithoutEnrollment[]> {
    return this.http.get<StudentWithoutEnrollment[]>(
      `${this.apiUrl}/processes/${processId}/students-without-enrollment`,
    );
  }
}