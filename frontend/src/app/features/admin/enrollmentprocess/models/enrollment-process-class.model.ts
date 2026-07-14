export interface EnrollmentProcessClass {
  id?: string;
  enrollmentProcessId: string;
  enrollmentProcessTitle?: string;

  classGroupId: string;
  classGroupCode?: string;
  classGroupName?: string;

  active?: boolean;
}