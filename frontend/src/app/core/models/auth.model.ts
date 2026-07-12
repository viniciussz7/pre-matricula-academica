export interface LoginRequest {
  email: string;
  password?: string;
}

export interface LoginResponse {
  token: string;
  role: 'ADMIN' | 'STUDENT';
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