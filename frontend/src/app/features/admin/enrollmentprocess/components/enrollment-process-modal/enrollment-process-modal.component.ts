import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EnrollmentProcessService } from '../../services/enrollment-process.service';
import { AcademicPeriodService } from '../../../academicperiod/services/academic-period.service';
import { EnrollmentProcess } from '../../models/enrollment-process.model';

@Component({
  selector: 'app-enrollment-process-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './enrollment-process-modal.html'
})
export class EnrollmentProcessModalComponent implements OnInit, OnChanges {
  @Input() processToEdit: EnrollmentProcess | null = null;
  @Output() closeModal = new EventEmitter<void>();
  @Output() saveSuccess = new EventEmitter<void>();

  form: FormGroup;
  isEditMode = false;
  isSubmitting = false;
  errorMessage: string | null = null;

  allPeriods: any[] = [];
  availablePeriods: any[] = [];
  allProcesses: EnrollmentProcess[] = [];
  minDate: string = '';

  constructor(
    private fb: FormBuilder,
    private service: EnrollmentProcessService,
    private periodService: AcademicPeriodService,
    private cdr: ChangeDetectorRef
  ) {
    this.minDate = new Date().toISOString().split('T')[0];
    this.form = this.fb.group({
      title: ['', Validators.required],
      academicPeriodId: ['', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      active: [true]
    });
  }

  ngOnInit() {
    this.loadInitialData();
  }

  loadInitialData() {
    this.periodService.getAll().subscribe(periods => {
      this.allPeriods = periods.filter(p => p.active === true);
      this.service.findAll().subscribe(processes => {
        this.allProcesses = processes;
        this.applyPeriodFilter();
      });
    });
  }

  applyPeriodFilter() {
    const usedPeriodIds = this.allProcesses
      .filter(p => p.active && p.id !== this.processToEdit?.id)
      .map(p => p.academicPeriodId);

    this.availablePeriods = this.allPeriods.filter(p =>
      !usedPeriodIds.includes(p.id) || p.id === this.processToEdit?.academicPeriodId
    );

    this.cdr.detectChanges();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.errorMessage = null;
    if (changes['processToEdit'] && this.processToEdit) {
      this.isEditMode = true;
      this.form.get('academicPeriodId')?.disable();

      const formatDateTime = (isoString: string | undefined) => {
        if (!isoString) return '';
        return isoString.substring(0, 16);
      };

      const formData = {
        ...this.processToEdit,
        startDate: formatDateTime(this.processToEdit.startDate),
        endDate: formatDateTime(this.processToEdit.endDate)
      };
      this.form.patchValue(formData);
    } else {
      this.isEditMode = false;
      this.form.get('academicPeriodId')?.enable();
      this.form.reset({ active: true });
    }
  }

  onSubmit() {
    if (this.form.invalid) return;

    this.isSubmitting = true;
    const values = this.form.getRawValue();

    const payload = {
      ...values,
      startDate: values.startDate ? `${values.startDate}:00` : null,
      endDate: values.endDate ? `${values.endDate}:00` : null
    };

    const request$ = (this.isEditMode && this.processToEdit?.id)
      ? this.service.update(this.processToEdit.id, payload)
      : this.service.create(payload);

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
      this.errorMessage = 'Já existe uma turma cadastrada com este código neste período letivo.';
    } else if (err.status === 400) {
      this.errorMessage = 'Dados inválidos. Verifique as regras de preenchimento (ex: Vagas > 0).';
    } else {
      this.errorMessage = 'Ocorreu um erro de comunicação com o servidor. Tente novamente.';
    }
  }
}