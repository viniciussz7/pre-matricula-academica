import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
} from '@angular/core';

import { CommonModule } from '@angular/common';

import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';

import { AdminService } from '../../services/admin.service';
import { Admin } from '../../models/admin.model';

@Component({
  selector: 'app-admin-modal',
  standalone: true,

  imports: [
    CommonModule,
    ReactiveFormsModule,
  ],

  templateUrl: './admin-modal.html',
})
export class AdminModalComponent
  implements OnChanges {

  @Input()
  adminToEdit: Admin | null = null;

  @Output()
  closeModal = new EventEmitter<void>();

  @Output()
  saveSuccess = new EventEmitter<void>();

  form: FormGroup;

  isEditMode = false;
  isSubmitting = false;

  errorMessage: string | null = null;

  constructor(
    private readonly formBuilder:
      FormBuilder,

    private readonly adminService:
      AdminService,
  ) {
    this.form = this.formBuilder.group({
      fullName: [
        '',
        [
          Validators.required,
          Validators.minLength(3),
        ],
      ],

      email: [
        '',
        [
          Validators.required,
          Validators.email,
        ],
      ],

      active: [true],
    });
  }

  ngOnChanges(
    changes: SimpleChanges,
  ): void {
    this.errorMessage = null;

    if (
      changes['adminToEdit'] &&
      this.adminToEdit
    ) {
      this.isEditMode = true;

      this.form.patchValue({
        fullName:
          this.adminToEdit.fullName,

        email:
          this.adminToEdit.email,

        active:
          this.adminToEdit.active ?? true,
      });

      return;
    }

    this.isEditMode = false;

    this.form.reset({
      fullName: '',
      email: '',
      active: true,
    });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage = null;
    this.isSubmitting = true;

    const payload: Admin =
      this.form.getRawValue();

    const adminId =
      this.adminToEdit?.id;

    const request$ =
      this.isEditMode && adminId
        ? this.adminService.update(
            adminId,
            payload,
          )
        : this.adminService.create(
            payload,
          );

    request$.subscribe({
      next: () => {
        this.isSubmitting = false;
        this.saveSuccess.emit();
      },

      error: error => {
        this.isSubmitting = false;
        this.handleError(error);
      },
    });
  }

  private handleError(
    error: any,
  ): void {
    if (
      error.error &&
      typeof error.error === 'string'
    ) {
      this.errorMessage =
        error.error;

      return;
    }

    if (error.error?.message) {
      this.errorMessage =
        error.error.message;

      return;
    }

    if (
      Array.isArray(
        error.error?.errors,
      )
    ) {
      this.errorMessage =
        'Verifique os campos: ' +
        error.error.errors
          .map(
            (item: any) =>
              item.defaultMessage ||
              item.field,
          )
          .join(', ');

      return;
    }

    if (error.status === 409) {
      this.errorMessage =
        'Já existe um usuário cadastrado com este e-mail.';

      return;
    }

    if (error.status === 400) {
      this.errorMessage =
        'Os dados informados são inválidos.';

      return;
    }

    this.errorMessage =
      'Ocorreu um erro de comunicação com o servidor. Tente novamente.';
  }
}