export interface Admin {
  id?: string;
  userId?: string;

  fullName: string;
  email: string;

  active?: boolean;
}