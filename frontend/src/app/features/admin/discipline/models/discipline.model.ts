export interface Discipline {
  id?: string; 
  code: string;
  name: string;
  workload: number;
  prerequisites: string;
  active?: boolean;
}