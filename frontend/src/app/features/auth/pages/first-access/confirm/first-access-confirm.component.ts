import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { AuthService } from '../../../../../core/services/auth.service';

@Component({
  selector: 'app-first-access-confirm',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './first-access-confirm.html'
})
export class FirstAccessConfirmComponent implements OnInit {
  form: FormGroup;
  isSubmitting = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      token: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit() {
    // Pega o token da URL se existir
    const token = this.route.snapshot.queryParamMap.get('token');
    if (token) this.form.patchValue({ token });
  }

  // Validador de senhas iguais
  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password')?.value;
    const confirm = control.get('confirmPassword')?.value;
    return password === confirm ? null : { passwordsMismatch: true };
  }

  onSubmit() {
    if (this.form.invalid) return;

    this.isSubmitting = true;
    this.errorMessage = null;

    // Envie o form.value diretamente, ele já contém token, password e confirmPassword
    this.authService.confirmFirstAccess(this.form.value).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.successMessage = 'Senha definida com sucesso!';
        this.form.disable();
      },
      error: (err) => {
        this.isSubmitting = false;
        // Melhore a mensagem de erro para ver o que vem do servidor
        this.errorMessage = err.error?.message || 'Erro ao confirmar. Verifique o token.';
        console.error(err);
      }
    });
  }
}