import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DisciplineService } from '../../services/discipline.service';
import { Discipline } from '../../models/discipline.model';

@Component({
  selector: 'app-discipline-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './discipline-modal.html'
})
export class DisciplineModalComponent implements OnChanges {
  @Input() disciplineToEdit: Discipline | null = null;
  @Output() closeModal = new EventEmitter<void>();
  @Output() saveSuccess = new EventEmitter<void>();
  
  form: FormGroup;
  isEditMode = false;
  
  isSubmitting = false;
  errorMessage: string | null = null;

  constructor(private fb: FormBuilder, private service: DisciplineService) {
    this.form = this.fb.group({
      code: ['', Validators.required],
      name: ['', Validators.required],
      workload: [null, [Validators.required, Validators.min(1)]],
      prerequisites: [''],
      active: [true]
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.errorMessage = null;
    if (changes['disciplineToEdit'] && this.disciplineToEdit) {
      this.isEditMode = true;
      this.form.patchValue(this.disciplineToEdit);
    } else {
      this.isEditMode = false;
      this.form.reset({ active: true });
    }
  }

  onSubmit() {
    if (this.form.invalid) return;

    this.errorMessage = null;
    this.isSubmitting = true;

    const request$ = (this.isEditMode && this.disciplineToEdit?.id)
      ? this.service.update(this.disciplineToEdit.id, this.form.value)
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
    } else if (err.error && err.error.message) {
      this.errorMessage = err.error.message; 
    } else if (err.error && Array.isArray(err.error.errors)) {
      this.errorMessage = 'Verifique os campos: ' + err.error.errors.map((e: any) => e.defaultMessage || e.field).join(', ');
    } else if (err.status === 409) {
      this.errorMessage = 'Já existe uma disciplina cadastrada com estes dados.';
    } else if (err.status === 400) {
      this.errorMessage = 'Formato incorreto de dados. Preencha todos os campos corretamente.';
    } else {
      this.errorMessage = 'Ocorreu um erro de comunicação com o servidor.';
    }
  }
}