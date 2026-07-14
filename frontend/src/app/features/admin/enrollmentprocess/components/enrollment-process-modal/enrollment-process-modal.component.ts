import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
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
import {
  forkJoin,
  of,
  Subscription,
  switchMap,
} from 'rxjs';

import { EnrollmentProcessService } from '../../services/enrollment-process.service';
import { EnrollmentProcessClassService } from '../../services/enrollment-process-class.service';
import { AcademicPeriodService } from '../../../academicperiod/services/academic-period.service';
import { ClassGroupService } from '../../../classgroup/services/class-group.service';

import { EnrollmentProcess } from '../../models/enrollment-process.model';
import { EnrollmentProcessClass } from '../../models/enrollment-process-class.model';
import { ClassGroup } from '../../../classgroup/models/class-group.model';

@Component({
  selector: 'app-enrollment-process-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
  ],
  templateUrl: './enrollment-process-modal.html',
})
export class EnrollmentProcessModalComponent
  implements OnInit, OnChanges, OnDestroy {

  @Input()
  processToEdit: EnrollmentProcess | null = null;

  @Output()
  closeModal = new EventEmitter<void>();

  @Output()
  saveSuccess = new EventEmitter<void>();

  form: FormGroup;

  isEditMode = false;
  isSubmitting = false;
  isLoadingData = false;

  errorMessage: string | null = null;

  allPeriods: any[] = [];
  availablePeriods: any[] = [];
  allProcesses: EnrollmentProcess[] = [];

  allClassGroups: ClassGroup[] = [];
  availableClassGroups: ClassGroup[] = [];

  minDate = '';

  private periodChangeSubscription?: Subscription;

  constructor(
    private readonly fb: FormBuilder,
    private readonly service: EnrollmentProcessService,
    private readonly processClassService:
      EnrollmentProcessClassService,
    private readonly periodService:
      AcademicPeriodService,
    private readonly classGroupService:
      ClassGroupService,
    private readonly cdr: ChangeDetectorRef,
  ) {
    this.minDate =
      new Date().toISOString().slice(0, 16);

    this.form = this.fb.group({
      title: [
        '',
        Validators.required,
      ],

      academicPeriodId: [
        '',
        Validators.required,
      ],

      startDate: [
        '',
        Validators.required,
      ],

      endDate: [
        '',
        Validators.required,
      ],

      classGroupIds: [
        [],
        Validators.required,
      ],

      active: [true],
    });
  }

  ngOnInit(): void {
    this.loadInitialData();

    this.periodChangeSubscription =
      this.form
        .get('academicPeriodId')
        ?.valueChanges
        .subscribe(periodId => {
          this.filterClassGroupsByPeriod(periodId);

          /*
           * Ao trocar o período durante a criação,
           * limpa as turmas anteriormente selecionadas.
           */
          if (!this.isEditMode) {
            this.form
              .get('classGroupIds')
              ?.setValue([]);
          }
        });
  }

  ngOnDestroy(): void {
    this.periodChangeSubscription?.unsubscribe();
  }

  ngOnChanges(
    changes: SimpleChanges,
  ): void {
    this.errorMessage = null;

    if (
      changes['processToEdit'] &&
      this.processToEdit
    ) {
      this.isEditMode = true;

      this.form
        .get('academicPeriodId')
        ?.disable();

      /*
       * As turmas do processo serão gerenciadas
       * na tela principal durante a edição.
       */
      this.form
        .get('classGroupIds')
        ?.clearValidators();

      this.form
        .get('classGroupIds')
        ?.updateValueAndValidity();

      this.form.patchValue({
        title: this.processToEdit.title,

        academicPeriodId:
          this.processToEdit.academicPeriodId,

        startDate:
          this.formatDateTime(
            this.processToEdit.startDate,
          ),

        endDate:
          this.formatDateTime(
            this.processToEdit.endDate,
          ),

        active:
          this.processToEdit.active,
      });
    } else {
      this.isEditMode = false;

      this.form
        .get('academicPeriodId')
        ?.enable();

      this.form
        .get('classGroupIds')
        ?.setValidators(
          Validators.required,
        );

      this.form
        .get('classGroupIds')
        ?.updateValueAndValidity();

      this.form.reset({
        title: '',
        academicPeriodId: '',
        startDate: '',
        endDate: '',
        classGroupIds: [],
        active: true,
      });

      this.availableClassGroups = [];
    }
  }

  loadInitialData(): void {
    this.isLoadingData = true;

    forkJoin({
      periods:
        this.periodService.getAll(),

      processes:
        this.service.findAll(),

      classGroups:
        this.classGroupService.getAll(),
    }).subscribe({
      next: ({
        periods,
        processes,
        classGroups,
      }) => {
        this.allPeriods =
          periods.filter(
            period => period.active === true,
          );

        this.allProcesses =
          processes;

        this.allClassGroups =
          classGroups.filter(
            classGroup =>
              classGroup.active === true,
          );

        this.applyPeriodFilter();

        const selectedPeriodId =
          this.form.get(
            'academicPeriodId',
          )?.value;

        if (selectedPeriodId) {
          this.filterClassGroupsByPeriod(
            selectedPeriodId,
          );
        }

        this.isLoadingData = false;
        this.cdr.detectChanges();
      },

      error: error => {
        console.error(
          'Erro ao carregar dados do modal:',
          error,
        );

        this.errorMessage =
          'Não foi possível carregar períodos e turmas.';

        this.isLoadingData = false;
        this.cdr.detectChanges();
      },
    });
  }

  applyPeriodFilter(): void {
    const usedPeriodIds =
      this.allProcesses
        .filter(process =>
          process.active &&
          process.id !==
            this.processToEdit?.id,
        )
        .map(process =>
          process.academicPeriodId,
        );

    this.availablePeriods =
      this.allPeriods.filter(period =>
        !usedPeriodIds.includes(period.id) ||
        period.id ===
          this.processToEdit
            ?.academicPeriodId,
      );
  }

  filterClassGroupsByPeriod(
    periodId: string,
  ): void {
    this.availableClassGroups =
      this.allClassGroups.filter(
        classGroup =>
          classGroup.academicPeriodId ===
          periodId,
      );

    this.cdr.detectChanges();
  }

  toggleClassGroup(
    classGroupId: string,
  ): void {
    const control =
      this.form.get('classGroupIds');

    const currentIds: string[] =
      control?.value ?? [];

    const updatedIds =
      currentIds.includes(classGroupId)
        ? currentIds.filter(
            id => id !== classGroupId,
          )
        : [
            ...currentIds,
            classGroupId,
          ];

    control?.setValue(updatedIds);
    control?.markAsTouched();
  }

  isClassGroupSelected(
    classGroupId: string,
  ): boolean {
    const selectedIds: string[] =
      this.form.get(
        'classGroupIds',
      )?.value ?? [];

    return selectedIds.includes(
      classGroupId,
    );
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage = null;
    this.isSubmitting = true;

    const values =
      this.form.getRawValue();

    const {
      classGroupIds,
      ...processValues
    } = values;

    const payload = {
      ...processValues,

      startDate:
        processValues.startDate
          ? `${processValues.startDate}:00`
          : null,

      endDate:
        processValues.endDate
          ? `${processValues.endDate}:00`
          : null,
    };

    if (
      this.isEditMode &&
      this.processToEdit?.id
    ) {
      this.service
        .update(
          this.processToEdit.id,
          payload,
        )
        .subscribe({
          next: () => {
            this.isSubmitting = false;
            this.saveSuccess.emit();
          },

          error: error => {
            this.isSubmitting = false;
            this.handleError(error);
          },
        });

      return;
    }

    this.service
      .create(payload)
      .pipe(
        switchMap(
          createdProcess => {

            const selectedIds:
              string[] =
                classGroupIds ?? [];

            if (
              selectedIds.length === 0
            ) {
              return of([]);
            }

            const requests =
              selectedIds.map(
                classGroupId =>
                  this.processClassService
                    .create({
                      enrollmentProcessId:
                        createdProcess.id,

                      classGroupId,

                      active: true,
                    } as EnrollmentProcessClass),
              );

            return forkJoin(requests);
          },
        ),
      )
      .subscribe({
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

  private formatDateTime(
    isoString?: string,
  ): string {
    if (!isoString) {
      return '';
    }

    return isoString.substring(0, 16);
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

    } else if (
      error.error?.message
    ) {
      this.errorMessage =
        error.error.message;

    } else if (
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

    } else if (
      error.status === 409
    ) {
      this.errorMessage =
        'Já existe um processo para o período informado.';

    } else if (
      error.status === 400
    ) {
      this.errorMessage =
        'Os dados informados são inválidos.';

    } else {
      this.errorMessage =
        'Ocorreu um erro de comunicação com o servidor.';
    }

    this.cdr.detectChanges();
  }
}