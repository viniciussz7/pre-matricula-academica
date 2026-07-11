import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AcademicPeriodService } from '../../services/academic-period.service';
import { AcademicPeriod } from '../../models/academic-period.model';

@Component({
  selector: 'app-academic-period-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './academic-period-modal.html'
})
export class AcademicPeriodModalComponent implements OnChanges {
  @Input() periodToEdit: AcademicPeriod | null = null;
  @Output() closeModal = new EventEmitter<void>();
  @Output() saveSuccess = new EventEmitter<void>();
  
  form: FormGroup;
  isEditMode = false;

  isSubmitting = false;
  errorMessage: string | null = null;

  constructor(private fb: FormBuilder, private service: AcademicPeriodService) {
    this.form = this.fb.group({
      code: ['', Validators.required],
      name: ['', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      active: [true]
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.errorMessage = null;
    if (changes['periodToEdit'] && this.periodToEdit) {
      this.isEditMode = true;
      this.form.patchValue(this.periodToEdit);
    } else {
      this.isEditMode = false;
      this.form.reset({ active: true });
    }
  }

  onSubmit() {
    if (this.form.invalid) return;

    this.errorMessage = null;
    this.isSubmitting = true;

    const request$ = (this.isEditMode && this.periodToEdit?.id)
      ? this.service.update(this.periodToEdit.id, this.form.value)
      : this.service.create(this.form.value);

    request$.subscribe({
      next: () => {
        this.isSubmitting = false;
        this.saveSuccess.emit();
      },
      error: (err) => {
        this.isSubmitting = false;
        this.handleError(err);
      }
    });
  }


private handleError(err: any) {
    if (err.error && typeof err.error === 'string') {
      this.errorMessage = err.error; 
    } 
    else if (err.error && err.error.message) {
      this.errorMessage = err.error.message; 
    } 
    else if (err.error && Array.isArray(err.error.errors)) {
      this.errorMessage = 'Verifique os campos: ' + err.error.errors.map((e: any) => e.defaultMessage || e.field).join(', ');
    } 
    else if (err.status === 409) {
      this.errorMessage = 'Já existe um período cadastrado com este código. Por favor, utilize outro.';
    } 
    else {
      this.errorMessage = 'Ocorreu um erro de comunicação com o servidor. Tente novamente.';
    }
  }
}