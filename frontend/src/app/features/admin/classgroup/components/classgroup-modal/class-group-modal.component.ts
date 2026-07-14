import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ClassGroupService } from '../../services/class-group.service';
import { ClassGroup } from '../../models/class-group.model';

// Importe os serviços de disciplina e período letivo:
import { DisciplineService } from '../../../discipline/services/discipline.service';
import { AcademicPeriodService } from '../../../academicperiod/services/academic-period.service';

@Component({
  selector: 'app-class-group-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './class-group-modal.html'
})
export class ClassGroupModalComponent implements OnInit, OnChanges {
  @Input() classGroupToEdit: ClassGroup | null = null;
  @Output() closeModal = new EventEmitter<void>();
  @Output() saveSuccess = new EventEmitter<void>();
  
  form: FormGroup;
  isEditMode = false;
  
  isSubmitting = false;
  errorMessage: string | null = null;

  disciplines: any[] = [];
  periods: any[] = [];

  constructor(
    private fb: FormBuilder, 
    private service: ClassGroupService,
    private disciplineService: DisciplineService,
    private periodService: AcademicPeriodService,
    private cdr: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      code: ['', Validators.required],
      name: ['', Validators.required],
      disciplineId: ['', Validators.required],
      academicPeriodId: ['', Validators.required],
      vacancies: [null, [Validators.required, Validators.min(1)]],
      allowOversubscription: [false],
      active: [true]
    });
  }

  ngOnInit() {
    // Carrega as opções para os campos Select
    this.loadDisciplines();
    this.loadPeriods();
  }

  loadDisciplines() {
    this.disciplineService.getAll().subscribe({
      next: (data) => {
        this.disciplines = data.filter(discipline => discipline.active === true);
      },
      error: (err) => {
        console.error('Erro ao buscar disciplinas', err);
      }
    });
    this.cdr.detectChanges();
  }

  loadPeriods() {
    this.periodService.getAll().subscribe({
      next: (data) => {
        this.periods = data.filter( period => period.active === true);
      },
      error: (err) => {
        console.error('Erro ao buscar períodos', err);
      }
    });
    this.cdr.detectChanges();
  }


  ngOnChanges(changes: SimpleChanges): void {
    this.errorMessage = null;
    if (changes['classGroupToEdit'] && this.classGroupToEdit) {
      this.isEditMode = true;
      this.form.patchValue(this.classGroupToEdit);
    } else {
      this.isEditMode = false;
      this.form.reset({ active: true, allowOversubscription: false });
    }
  }

  onSubmit() {
    if (this.form.invalid) return;

    this.errorMessage = null;
    this.isSubmitting = true;

    const request$ = (this.isEditMode && this.classGroupToEdit?.id)
      ? this.service.update(this.classGroupToEdit.id, this.form.value)
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
      this.errorMessage = 'Já existe uma turma cadastrada com este código neste período letivo.';
    } else if (err.status === 400) {
      this.errorMessage = 'Dados inválidos. Verifique as regras de preenchimento (ex: Vagas > 0).';
    } else {
      this.errorMessage = 'Ocorreu um erro de comunicação com o servidor. Tente novamente.';
    }
  }
}