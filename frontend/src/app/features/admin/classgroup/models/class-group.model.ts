export interface ClassGroup {
    id: string;
    code: string;
    name: string;
    disciplineId: string;
    academicPeriodId: string;
    vacancies: number;
    allowOversubscription: boolean;
    active?: boolean;
}