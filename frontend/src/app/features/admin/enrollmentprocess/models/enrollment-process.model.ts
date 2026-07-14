export interface EnrollmentProcess {
  id?: string;
  title: string;
  academicPeriodId: string;
  startDate: string;
  endDate: string;
  active?: boolean;
}