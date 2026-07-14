import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import {
  catchError,
  finalize,
  forkJoin,
  of,
} from 'rxjs';

interface EnrollmentItem {
  id: string;
  enrollmentProcessClassId: string;
  classGroupId: string;
  classGroupCode: string;
  classGroupName: string;
}

interface Enrollment {
  id: string;
  studentId: string;
  enrollmentProcessId: string;
  enrollmentProcessTitle: string;
  items: EnrollmentItem[];
  totalItems: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

interface EnrollmentProcessClass {
  id: string;
  enrollmentProcessId: string;
  enrollmentProcessTitle: string;
  classGroupId: string;
  classGroupCode: string;
  classGroupName: string;
  active: boolean;
}

@Component({
  selector: 'app-my-history',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
  ],
  templateUrl: './my-history.html',
})
export class MyHistoryComponent implements OnInit {

  private readonly enrollmentApiUrl =
    'http://localhost:8080/enrollments';

  private readonly openClassesApiUrl =
    'http://localhost:8080/enrollment-processes/open/classes';

  currentEnrollment: Enrollment | null = null;
  enrollmentHistory: Enrollment[] = [];

  availableClasses: EnrollmentProcessClass[] = [];

  isLoading = true;
  isEditing = false;
  isSaving = false;
  isLoadingClasses = false;

  hasHistoryError = false;
  editErrorMessage = '';
  editSuccessMessage = '';

  selectedProcessClassIds: string[] = [];
  classIdToAdd = '';

  constructor(
    private readonly http: HttpClient,
    private readonly changeDetectorRef: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadEnrollments();
  }

  loadEnrollments(): void {
    this.isLoading = true;
    this.hasHistoryError = false;

    forkJoin({
      currentEnrollment: this.http
        .get<Enrollment>(`${this.enrollmentApiUrl}/me`)
        .pipe(
          catchError(() => of(null)),
        ),

      enrollmentHistory: this.http
        .get<Enrollment[]>(
          `${this.enrollmentApiUrl}/me/history`,
        )
        .pipe(
          catchError(() => {
            this.hasHistoryError = true;
            return of([]);
          }),
        ),
    })
      .pipe(
        finalize(() => {
          this.isLoading = false;

          /*
           * Força a atualização da tela.
           * Isso também deve corrigir o problema de precisar
           * clicar novamente no menu para sair do carregamento.
           */
          this.changeDetectorRef.detectChanges();
        }),
      )
      .subscribe({
        next: ({
          currentEnrollment,
          enrollmentHistory,
        }) => {
          this.currentEnrollment = currentEnrollment;
          this.enrollmentHistory = enrollmentHistory;
        },

        error: () => {
          this.hasHistoryError = true;
        },
      });
  }

  startEditing(): void {
    if (
      !this.currentEnrollment ||
      !this.currentEnrollment.active
    ) {
      return;
    }

    this.editErrorMessage = '';
    this.editSuccessMessage = '';
    this.classIdToAdd = '';

    this.selectedProcessClassIds =
      this.currentEnrollment.items.map(
        item => item.enrollmentProcessClassId,
      );

    this.isEditing = true;
    this.loadAvailableClasses();
  }

  cancelEditing(): void {
    this.isEditing = false;
    this.classIdToAdd = '';
    this.selectedProcessClassIds = [];
    this.editErrorMessage = '';
  }

  loadAvailableClasses(): void {
    this.isLoadingClasses = true;

    this.http
      .get<EnrollmentProcessClass[]>(
        this.openClassesApiUrl,
      )
      .pipe(
        catchError(() => {
          this.editErrorMessage =
            'Não foi possível carregar as turmas disponíveis.';

          return of([]);
        }),

        finalize(() => {
          this.isLoadingClasses = false;
          this.changeDetectorRef.detectChanges();
        }),
      )
      .subscribe(classes => {
        this.availableClasses = classes.filter(
          processClass => processClass.active,
        );
      });
  }

  addSelectedClass(): void {
    if (!this.classIdToAdd) {
      return;
    }

    if (
      this.selectedProcessClassIds.includes(
        this.classIdToAdd,
      )
    ) {
      this.editErrorMessage =
        'Esta turma já está na pré-matrícula.';

      return;
    }

    if (this.selectedProcessClassIds.length >= 7) {
      this.editErrorMessage =
        'A pré-matrícula pode possuir no máximo 7 turmas.';

      return;
    }

    this.selectedProcessClassIds = [
      ...this.selectedProcessClassIds,
      this.classIdToAdd,
    ];

    this.classIdToAdd = '';
    this.editErrorMessage = '';
  }

  removeSelectedClass(
    enrollmentProcessClassId: string,
  ): void {
    this.selectedProcessClassIds =
      this.selectedProcessClassIds.filter(
        id => id !== enrollmentProcessClassId,
      );

    this.editErrorMessage = '';
  }

  saveEnrollment(): void {
    if (!this.currentEnrollment) {
      return;
    }

    if (this.selectedProcessClassIds.length === 0) {
      this.editErrorMessage =
        'A pré-matrícula deve possuir pelo menos uma turma.';

      return;
    }

    if (this.selectedProcessClassIds.length > 7) {
      this.editErrorMessage =
        'A pré-matrícula pode possuir no máximo 7 turmas.';

      return;
    }

    this.isSaving = true;
    this.editErrorMessage = '';
    this.editSuccessMessage = '';

    const body = {
      enrollmentProcessClassIds:
        this.selectedProcessClassIds,
    };

    this.http
      .put<Enrollment>(
        `${this.enrollmentApiUrl}/${this.currentEnrollment.id}`,
        body,
      )
      .pipe(
        finalize(() => {
          this.isSaving = false;
          this.changeDetectorRef.detectChanges();
        }),
      )
      .subscribe({
        next: updatedEnrollment => {
          this.currentEnrollment = updatedEnrollment;

          this.editSuccessMessage =
            'Pré-matrícula atualizada com sucesso.';

          this.isEditing = false;
          this.classIdToAdd = '';

          /*
           * Recarrega também o histórico para refletir
           * imediatamente a atualização.
           */
          this.loadEnrollments();
        },

        error: error => {
          this.editErrorMessage =
            error?.error?.message ??
            'Não foi possível atualizar a pré-matrícula.';
        },
      });
  }

  isClassSelected(
    enrollmentProcessClassId: string,
  ): boolean {
    return this.selectedProcessClassIds.includes(
      enrollmentProcessClassId,
    );
  }

  getSelectedClasses(): EnrollmentProcessClass[] {
    return this.availableClasses.filter(processClass =>
      this.selectedProcessClassIds.includes(
        processClass.id,
      ),
    );
  }

  getAvailableClassesToAdd():
    EnrollmentProcessClass[] {

    return this.availableClasses.filter(
      processClass =>
        !this.selectedProcessClassIds.includes(
          processClass.id,
        ),
    );
  }

  getStatusLabel(enrollment: Enrollment): string {
    return enrollment.active
      ? 'Ativa'
      : 'Cancelada';
  }

  trackEnrollmentById(
    _index: number,
    enrollment: Enrollment,
  ): string {
    return enrollment.id;
  }

  trackItemById(
    _index: number,
    item: EnrollmentItem,
  ): string {
    return item.id;
  }

  trackProcessClassById(
    _index: number,
    processClass: EnrollmentProcessClass,
  ): string {
    return processClass.id;
  }
}