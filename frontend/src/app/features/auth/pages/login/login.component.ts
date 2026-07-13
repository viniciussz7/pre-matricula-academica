import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html'
})
export class LoginComponent {
  credentials = { email: '', password: '' };
  
  private router = inject(Router);
  private authService = inject(AuthService);

  onLogin() {
    this.authService.login(this.credentials).subscribe({
      next: (response) => {
        if (response.role === 'ADMIN') {
          this.router.navigate(['/admin/dashboard']);
        } else {
          this.router.navigate(['/student/dashboard']);
        }
      },
      error: (err) => {
        alert('E-mail ou senha incorretos!');
      }
    });
  }

  goToFirstAccess() {
    this.router.navigate(['/auth/first-access/request']);
  }
}