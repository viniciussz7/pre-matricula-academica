export interface MostDemandedClass {
  enrollmentProcessClassId: string;
  classGroupId: string;
  classCode: string;
  className: string;
  enrolledStudents: number;
}

export interface ProcessSummary {
  enrollmentProcessId: string;
  enrollmentProcessTitle: string;

  totalEnrollments: number;
  activeEnrollments: number;
  cancelledEnrollments: number;

  totalClasses: number;
  totalSelections: number;

  averageClassesPerActiveEnrollment: number;

  fullClasses: number;
  oversubscribedClasses: number;

  mostDemandedClass: MostDemandedClass | null;
}

export interface ClassDemand {
  enrollmentProcessClassId: string;
  classGroupId: string;

  classCode: string;
  className: string;

  disciplineCode: string;
  disciplineName: string;

  vacancies: number;
  allowOversubscription: boolean;

  enrolledStudents: number;
  remainingVacancies: number;
  occupancyPercentage: number;
}

export interface EnrolledStudent {
  enrollmentItemId: string;
  enrollmentId: string;

  studentId: string;
  registrationNumber: string;

  studentName: string;
  studentEmail: string;

  selectedAt: string;
}

export interface StudentWithoutEnrollment {
  studentId: string;
  userId: string;

  registrationNumber: string;
  studentName: string;
  studentEmail: string;

  firstAccess: boolean;
}