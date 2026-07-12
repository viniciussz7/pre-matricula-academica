export interface LoginRequest {
  email: string;
  password?: string;
}

export interface LoginResponse {
  token: string;
  role: 'ADMIN' | 'STUDENT';
  user: AdminResponseDTO | StudentResponseDTO;
}

export interface FirstAccessRequestDTO {
  email: string;
  registrationNumber: string;
}

export interface FirstAccessDTO {
  token: string;
  password: string;
  confirmPassword: string;
}

export interface AdminResponseDTO {
  id: string;
  userId: string;
  fullName: string;
  email: string;
  active: boolean;
 }

 export interface StudentResponseDTO {
  id: string;
  userId: string;
  fullName: string;
  email: string;
  registrationNumber: string;
  active: boolean;
 }