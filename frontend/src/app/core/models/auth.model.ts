export interface LoginRequest {
  email: string;
  password?: string;
}

export interface LoginResponse {
  token: string;
  role: 'ADMIN' | 'STUDENT';
  fullName: string;
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