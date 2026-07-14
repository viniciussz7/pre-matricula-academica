import {
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  finalize,
  forkJoin,
} from 'rxjs';

import { EnrollmentProcessService } from '../../../enrollmentprocess/services/enrollment-process.service';

import { EnrollmentProcess } from '../../../enrollmentprocess/models/enrollment-process.model';

import { ReportService } from '../../services/report.service';

import {
  ClassDemand,
  EnrolledStudent,
  ProcessSummary,
  StudentWithoutEnrollment,
} from '../../models/report.model';

@Component({
  selector: 'app-reports',
  standalone: true,

  imports: [
    CommonModule,
    FormsModule,
  ],

  templateUrl: 'reports.html',
})
export class ReportsComponent implements OnInit {
  processes: EnrollmentProcess[] = [];

  selectedProcessId = '';
  selectedProcess: EnrollmentProcess | null = null;

  summary: ProcessSummary | null = null;
  classDemand: ClassDemand[] = [];
  studentsWithoutEnrollment:
    StudentWithoutEnrollment[] = [];

  selectedProcessClassId = '';
  enrolledStudents: EnrolledStudent[] = [];

  isLoadingProcesses = false;
  isLoadingReports = false;
  isLoadingStudents = false;

  processErrorMessage: string | null = null;
  reportErrorMessage: string | null = null;
  studentErrorMessage: string | null = null;

  constructor(
    private readonly enrollmentProcessService:
      EnrollmentProcessService,

    private readonly reportService:
      ReportService,

    private readonly cdr:
      ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadProcesses();
  }

  loadProcesses(): void {
    this.isLoadingProcesses = true;
    this.processErrorMessage = null;

    this.enrollmentProcessService
      .findAll()
      .pipe(
        finalize(() => {
          this.isLoadingProcesses = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: processes => {
          this.processes = [...processes]
            .filter(
              (
                process,
              ): process is EnrollmentProcess & {
                id: string;
              } => typeof process.id === 'string',
            )
            .sort(
              (first, second) =>
                new Date(
                  second.startDate,
                ).getTime() -
                new Date(
                  first.startDate,
                ).getTime(),
            );
        },

        error: error => {
          console.error(
            'Erro ao carregar processos:',
            error,
          );

          this.processErrorMessage =
            'Não foi possível carregar os processos de pré-matrícula.';
        },
      });
  }

  onProcessChange(): void {
    this.clearReports();

    if (!this.selectedProcessId) {
      this.selectedProcess = null;
      return;
    }

    this.selectedProcess =
      this.processes.find(
        process =>
          process.id === this.selectedProcessId,
      ) ?? null;

    this.loadProcessReports();
  }

  loadProcessReports(): void {
    const processId =
      this.selectedProcessId;

    if (!processId) {
      return;
    }

    this.isLoadingReports = true;
    this.reportErrorMessage = null;

    forkJoin({
      summary:
        this.reportService.findProcessSummary(
          processId,
        ),

      classDemand:
        this.reportService.findClassDemand(
          processId,
        ),

      studentsWithoutEnrollment:
        this.reportService
          .findStudentsWithoutEnrollment(
            processId,
          ),
    })
      .pipe(
        finalize(() => {
          this.isLoadingReports = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: ({
          summary,
          classDemand,
          studentsWithoutEnrollment,
        }) => {
          this.summary = summary;

          this.classDemand =
            [...classDemand].sort(
              (first, second) =>
                second.enrolledStudents -
                first.enrolledStudents,
            );

          this.studentsWithoutEnrollment =
            [...studentsWithoutEnrollment].sort(
              (first, second) =>
                first.studentName.localeCompare(
                  second.studentName,
                  'pt-BR',
                  {
                    sensitivity: 'base',
                  },
                ),
            );
        },

        error: error => {
          console.error(
            'Erro ao carregar relatórios:',
            error,
          );

          this.reportErrorMessage =
            error?.error?.message ??
            'Não foi possível carregar os relatórios do processo.';
        },
      });
  }

  onProcessClassChange(): void {
    this.enrolledStudents = [];
    this.studentErrorMessage = null;

    const processClassId =
      this.selectedProcessClassId;

    if (!processClassId) {
      return;
    }

    this.isLoadingStudents = true;

    this.reportService
      .findStudentsByProcessClass(
        processClassId,
      )
      .pipe(
        finalize(() => {
          this.isLoadingStudents = false;
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: students => {
          this.enrolledStudents =
            [...students].sort(
              (first, second) =>
                first.studentName.localeCompare(
                  second.studentName,
                  'pt-BR',
                  {
                    sensitivity: 'base',
                  },
                ),
            );
        },

        error: error => {
          console.error(
            'Erro ao carregar alunos da turma:',
            error,
          );

          this.studentErrorMessage =
            error?.error?.message ??
            'Não foi possível carregar os alunos inscritos na turma.';
        },
      });
  }

  getSelectedClassDemand():
    ClassDemand | null {
    return (
      this.classDemand.find(
        classDemand =>
          classDemand.enrollmentProcessClassId ===
          this.selectedProcessClassId,
      ) ?? null
    );
  }

  getOccupancyClasses(
    classDemand: ClassDemand,
  ): string {
    if (
      classDemand.enrolledStudents >
      classDemand.vacancies
    ) {
      return 'bg-red-100 text-red-700';
    }

    if (
      classDemand.enrolledStudents ===
      classDemand.vacancies
    ) {
      return 'bg-yellow-100 text-yellow-700';
    }

    return 'bg-green-100 text-green-700';
  }

  getOccupancyLabel(
    classDemand: ClassDemand,
  ): string {
    if (
      classDemand.enrolledStudents >
      classDemand.vacancies
    ) {
      return 'Excedida';
    }

    if (
      classDemand.enrolledStudents ===
      classDemand.vacancies
    ) {
      return 'Lotada';
    }

    return 'Disponível';
  }

  getProcessStatus(): string {
    const process =
      this.selectedProcess;

    if (!process) {
      return '';
    }

    if (!process.active) {
      return 'Inativo';
    }

    const now = new Date().getTime();

    const start =
      new Date(
        process.startDate,
      ).getTime();

    const end =
      new Date(
        process.endDate,
      ).getTime();

    if (now < start) {
      return 'Não iniciado';
    }

    if (now > end) {
      return 'Encerrado';
    }

    return 'Aberto';
  }

  private clearReports(): void {
    this.summary = null;
    this.classDemand = [];
    this.studentsWithoutEnrollment = [];

    this.selectedProcessClassId = '';
    this.enrolledStudents = [];

    this.reportErrorMessage = null;
    this.studentErrorMessage = null;
  }
}