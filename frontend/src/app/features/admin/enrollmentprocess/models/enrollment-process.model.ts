export interface EnrollmentProcess {
    id?: string;
    title: string;
    academicPeriodId: string;
    startDate: string;
    endDate: string;
    active?: boolean;
}

export interface EnrollmentProcessClass {
    id?: string;
    enrollmentProcessId: string;
    classGroup: string;
    active?: boolean;
}

export interface Enrollment {
    id?: string;
    studentId: string;
    enrollmentProcessId: string;
    items: EnrollmentItem[]; 
    active?: boolean;
}

export interface EnrollmentItem {
    id?: string;
    enrollmentId: string;
    enrollmentProcessClassId: string;
}
