import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { FormsModule } from '@angular/forms';

@Component({
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html'
})
export class LoginComponent {
  credentials = { email: '', password: '' };
  private authService = inject(AuthService);
  private router = inject(Router);

  onLogin() {
    // TODO: Chamar o authService.login(this.credentials) aqui
    console.log('Login realizado!');
    
    // Simulação de sucesso para navegar
    this.router.navigate(['/dashboard']);
  }

  goToForgotPassword() {
    // TODO: Navegar para tela de recuperação de senha (ACHO QUE NAO TEREMOS)
    console.log('Redirecionar para recuperação de senha');
  }

  goToFirstAccess() {
    // TODO: Navegar para tela de primeiro acesso
    console.log('Redirecionar para primeiro acesso');
  }
}