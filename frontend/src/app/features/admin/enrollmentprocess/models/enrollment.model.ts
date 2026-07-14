import { EnrollmentItem } from './enrollment-item.model';

export interface Enrollment {
  id?: string;

  studentId: string;

  enrollmentProcessId: string;
  enrollmentProcessTitle?: string;

  items: EnrollmentItem[];

  totalItems?: number;

  active?: boolean;

  createdAt?: string;
  updatedAt?: string;
}