export interface Enrollment {
    id?: string;
    studentId: string;
    enrollmentProcessId: string;
    items: string;
    active?: boolean;
}