import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../../../core/services/auth.service';

@Component({
  selector: 'app-first-access-request',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './first-access-request.html'
})
export class FirstAccessRequestComponent {
  form: FormGroup;
  isSubmitting = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      registrationNumber: ['', Validators.required]
    });
  }

  onSubmit() {
    if (this.form.invalid) return;

    this.isSubmitting = true;
    this.errorMessage = null;
    this.successMessage = null;

    this.authService.requestFirstAccess(this.form.value).subscribe({
      next: (response) => {
        this.isSubmitting = false;
        // Redireciona passando o token como parâmetro na URL
        this.router.navigate(['/auth/first-access/confirm'], {
        });
      },
      error: (err) => {
        this.isSubmitting = false;
        this.errorMessage = 'Dados não encontrados. Verifique seu E-mail e Matrícula.';
        console.error(err);
      }
    });
  }
}