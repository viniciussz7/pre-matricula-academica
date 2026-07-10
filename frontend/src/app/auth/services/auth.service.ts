import { Injectable } from '@angular/core';
import { LoginCredentials } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  
  login(credentials: LoginCredentials) {
    // TODO: Implementar lógica de chamada HTTP (HttpClient)
    // Ex: return this.http.post('/api/login', credentials);
    console.log('Tentativa de login com:', credentials);
    return true; 
  }
}