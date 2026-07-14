import {
  ChangeDetectorRef,
  Component,
  OnDestroy,
  OnInit,
} from '@angular/core';

import {
  CommonModule,
  DatePipe,
} from '@angular/common';

import { FormsModule } from '@angular/forms';

import {
  forkJoin,
  Subscription,
} from 'rxjs';

import { EnrollmentProcessService } from '../../services/enrollment-process.service';

import {
  CreateEnrollmentProcessClassRequest,
  EnrollmentProcessClassService,
} from '../../services/enrollment-process-class.service';

import { AcademicPeriodService } from '../../../academicperiod/services/academic-period.service';

import { ClassGroupService } from '../../../classgroup/services/class-group.service';

import { EnrollmentProcess } from '../../models/enrollment-process.model';

import { EnrollmentProcessClass } from '../../models/enrollment-process-class.model';

import { ClassGroup } from '../../../classgroup/models/class-group.model';

import { EnrollmentProcessModalComponent } from '../../components/enrollment-process-modal/enrollment-process-modal.component';

import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

type ProcessStatus =
  | 'NOT_STARTED'
  | 'OPEN'
  | 'CLOSED'
  | 'INACTIVE';

type PersistedClassGroup =
  ClassGroup & {
    id: string;
  };

type PersistedProcessClass =
  EnrollmentProcessClass & {
    id: string;
    classGroupId: string;
  };

@Component({
  selector: 'app-enrollment-process',
  standalone: true,

  imports: [
    CommonModule,
    FormsModule,
    EnrollmentProcessModalComponent,
  ],

  providers: [
    DatePipe,
  ],

  templateUrl: './enrollment-process.html',
})
export class EnrollmentProcessComponent
  implements OnInit, OnDestroy {

  processes: EnrollmentProcess[] = [];

  filteredProcesses:
    EnrollmentProcess[] = [];

  periods: any[] = [];

  allActiveClassGroups:
    ClassGroup[] = [];

  linkedClasses:
    EnrollmentProcessClass[] = [];

  availableClasses:
    PersistedClassGroup[] = [];

  selectedProcess:
    EnrollmentProcess | null = null;

  selectedClassGroupId = '';

  isModalOpen = false;

  isLoadingLinks = false;

  isEditingClasses = false;

  isSavingClasses = false;

  editableClassGroupIds:
    string[] = [];

  linkErrorMessage:
    string | null = null;

  linkSuccessMessage:
    string | null = null;

  searchTerm = '';

  private linkedClassesSub?:
    Subscription;

  constructor(
    private readonly service:
      EnrollmentProcessService,

    private readonly periodService:
      AcademicPeriodService,

    private readonly datePipe:
      DatePipe,

    private readonly classGroupService:
      ClassGroupService,

    private readonly cdr:
      ChangeDetectorRef,

    private readonly processClassService:
      EnrollmentProcessClassService,
  ) {}

  ngOnInit(): void {
    this.loadInitialData();
  }

  ngOnDestroy(): void {
    this.linkedClassesSub?.unsubscribe();
  }

  loadInitialData(): void {
    forkJoin({
      periods:
        this.periodService.getAll(),

      classGroups:
        this.classGroupService.getAll(),

      processes:
        this.service.findAll(),
    }).subscribe({
      next: ({
        periods,
        classGroups,
        processes,
      }) => {
        this.periods = periods;

        this.allActiveClassGroups =
          classGroups.filter(
            classGroup =>
              classGroup.active === true,
          );

        this.processes =
          this.sortProcesses(processes);

        this.onSearch();

        this.cdr.detectChanges();
      },

      error: error => {
        console.error(
          'Erro ao carregar os processos:',
          error,
        );
      },
    });
  }

  loadProcesses(): void {
    this.service.findAll().subscribe({
      next: processes => {
        this.processes =
          this.sortProcesses(processes);

        this.onSearch();

        const selectedProcessId =
          this.selectedProcess?.id;

        if (selectedProcessId) {
          this.selectedProcess =
            this.processes.find(
              process =>
                process.id ===
                selectedProcessId,
            ) ?? null;
        }

        this.cdr.detectChanges();
      },

      error: error => {
        console.error(
          'Erro ao carregar os processos:',
          error,
        );
      },
    });
  }

  private sortProcesses(
    processes: EnrollmentProcess[],
  ): EnrollmentProcess[] {
    return [...processes].sort(
      (first, second) => {
        const firstStart =
          new Date(
            first.startDate,
          ).getTime();

        const secondStart =
          new Date(
            second.startDate,
          ).getTime();

        return secondStart - firstStart;
      },
    );
  }

  onSearch(): void {
    const term =
      this.searchTerm
        .trim()
        .toLocaleLowerCase('pt-BR');

    this.filteredProcesses =
      this.processes.filter(process => {
        const periodText =
          this.getPeriodInfo(
            process.academicPeriodId,
          ).toLocaleLowerCase('pt-BR');

        return (
          process.title
            .toLocaleLowerCase('pt-BR')
            .includes(term) ||
          periodText.includes(term)
        );
      });
  }

  selectProcess(
    process: EnrollmentProcess,
  ): void {
    this.linkedClassesSub?.unsubscribe();

    this.selectedProcess = process;

    this.linkedClasses = [];

    this.availableClasses = [];

    this.editableClassGroupIds = [];

    this.selectedClassGroupId = '';

    this.isEditingClasses = false;

    this.linkErrorMessage = null;

    this.linkSuccessMessage = null;

    this.loadLinkedClasses();
  }

  loadLinkedClasses(): void {
    const processId =
      this.selectedProcess?.id;

    if (!processId) {
      return;
    }

    this.isLoadingLinks = true;

    this.linkedClassesSub =
      this.processClassService
        .findByProcess(processId)
        .subscribe({
          next: data => {
            this.linkedClasses = data;

            this.editableClassGroupIds =
              data
                .map(
                  link =>
                    link.classGroupId,
                )
                .filter(
                  (
                    id,
                  ): id is string =>
                    typeof id === 'string',
                );

            this.updateAvailableClasses();

            this.isLoadingLinks = false;

            this.cdr.detectChanges();
          },

          error: error => {
            console.error(
              'Erro ao buscar turmas vinculadas:',
              error,
            );

            this.linkErrorMessage =
              'Não foi possível carregar as turmas vinculadas.';

            this.isLoadingLinks = false;

            this.cdr.detectChanges();
          },
        });
  }

  updateAvailableClasses(): void {
    const selectedProcess =
      this.selectedProcess;

    if (!selectedProcess) {
      this.availableClasses = [];
      return;
    }

    this.availableClasses =
      this.allActiveClassGroups
        .filter(
          (
            classGroup,
          ): classGroup is PersistedClassGroup =>
            typeof classGroup.id ===
            'string',
        )
        .filter(
          classGroup =>
            classGroup.academicPeriodId ===
              selectedProcess.academicPeriodId &&
            !this.editableClassGroupIds.includes(
              classGroup.id,
            ),
        );
  }

  startEditingClasses(): void {
    const selectedProcess =
      this.selectedProcess;

    if (
      !selectedProcess ||
      !this.canManageClasses(
        selectedProcess,
      )
    ) {
      return;
    }

    this.editableClassGroupIds =
      this.linkedClasses
        .map(
          link =>
            link.classGroupId,
        )
        .filter(
          (
            id,
          ): id is string =>
            typeof id === 'string',
        );

    this.selectedClassGroupId = '';

    this.linkErrorMessage = null;

    this.linkSuccessMessage = null;

    this.updateAvailableClasses();

    this.isEditingClasses = true;
  }

  cancelEditingClasses(): void {
    this.editableClassGroupIds =
      this.linkedClasses
        .map(
          link =>
            link.classGroupId,
        )
        .filter(
          (
            id,
          ): id is string =>
            typeof id === 'string',
        );

    this.selectedClassGroupId = '';

    this.isEditingClasses = false;

    this.linkErrorMessage = null;

    this.updateAvailableClasses();
  }

  addClassToSelection(): void {
    const classGroupId =
      this.selectedClassGroupId;

    if (!classGroupId) {
      return;
    }

    if (
      !this.editableClassGroupIds.includes(
        classGroupId,
      )
    ) {
      this.editableClassGroupIds = [
        ...this.editableClassGroupIds,
        classGroupId,
      ];
    }

    this.selectedClassGroupId = '';

    this.updateAvailableClasses();
  }

  removeClassFromSelection(
    classGroupId: string,
  ): void {
    this.editableClassGroupIds =
      this.editableClassGroupIds.filter(
        id => id !== classGroupId,
      );

    this.updateAvailableClasses();
  }

  saveClassChanges(): void {
    const selectedProcess =
      this.selectedProcess;

    const processId =
      selectedProcess?.id;

    if (
      !selectedProcess ||
      !processId ||
      !this.canManageClasses(
        selectedProcess,
      )
    ) {
      return;
    }

    const originalClassIds =
      this.linkedClasses
        .map(
          link =>
            link.classGroupId,
        )
        .filter(
          (
            id,
          ): id is string =>
            typeof id === 'string',
        );

    const addedIds =
      this.editableClassGroupIds.filter(
        id =>
          !originalClassIds.includes(id),
      );

    const removedLinks =
      this.linkedClasses
        .filter(
          (
            link,
          ): link is PersistedProcessClass =>
            typeof link.id === 'string' &&
            typeof link.classGroupId ===
              'string',
        )
        .filter(
          link =>
            !this.editableClassGroupIds.includes(
              link.classGroupId,
            ),
        );

    const createRequests =
      addedIds.map(classGroupId => {
        const request:
          CreateEnrollmentProcessClassRequest = {
            enrollmentProcessId:
              processId,

            classGroupId,

            active: true,
          };

        return this.processClassService
          .create(request);
      });

    const deactivateRequests =
      removedLinks.map(link =>
        this.processClassService
          .deactivate(link.id),
      );

    const requests = [
      ...createRequests,
      ...deactivateRequests,
    ];

    if (requests.length === 0) {
      this.isEditingClasses = false;
      return;
    }

    this.isSavingClasses = true;

    this.linkErrorMessage = null;

    this.linkSuccessMessage = null;

    forkJoin(requests).subscribe({
      next: () => {
        this.isSavingClasses = false;

        this.isEditingClasses = false;

        this.linkSuccessMessage =
          'Turmas do processo atualizadas com sucesso.';

        this.loadLinkedClasses();
      },

      error: error => {
        console.error(
          'Erro ao atualizar as turmas:',
          error,
        );

        this.isSavingClasses = false;

        this.linkErrorMessage =
          error?.error?.message ??
          'Não foi possível salvar as alterações das turmas.';

        this.cdr.detectChanges();
      },
    });
  }

  getEditableClassGroups():
    PersistedClassGroup[] {
    return this.allActiveClassGroups
      .filter(
        (
          classGroup,
        ): classGroup is PersistedClassGroup =>
          typeof classGroup.id ===
          'string',
      )
      .filter(
        classGroup =>
          this.editableClassGroupIds.includes(
            classGroup.id,
          ),
      );
  }

  getClassGroupName(
    id: string,
  ): string {
    const classGroup =
      this.allActiveClassGroups.find(
        item => item.id === id,
      );

    return classGroup
      ? `${classGroup.code} — ${classGroup.name}`
      : 'Turma não encontrada';
  }

  getPeriodInfo(
    id: string,
  ): string {
    const period =
      this.periods.find(
        item => item.id === id,
      );

    return period
      ? `${period.code} — ${period.name}`
      : 'Período não encontrado';
  }

  getProcessStatus(
    process: EnrollmentProcess,
  ): ProcessStatus {
    if (!process.active) {
      return 'INACTIVE';
    }

    const now =
      new Date().getTime();

    const start =
      new Date(
        process.startDate,
      ).getTime();

    const end =
      new Date(
        process.endDate,
      ).getTime();

    if (now < start) {
      return 'NOT_STARTED';
    }

    if (now > end) {
      return 'CLOSED';
    }

    return 'OPEN';
  }

  getStatusLabel(
    process: EnrollmentProcess,
  ): string {
    const labels:
      Record<ProcessStatus, string> = {
        NOT_STARTED:
          'Não iniciado',

        OPEN:
          'Aberto',

        CLOSED:
          'Encerrado',

        INACTIVE:
          'Inativo',
      };

    return labels[
      this.getProcessStatus(process)
    ];
  }

  getStatusClasses(
    process: EnrollmentProcess,
  ): string {
    const classes:
      Record<ProcessStatus, string> = {
        NOT_STARTED:
          'bg-yellow-100 text-yellow-800',

        OPEN:
          'bg-green-100 text-green-800',

        CLOSED:
          'bg-gray-200 text-gray-700',

        INACTIVE:
          'bg-red-100 text-red-800',
      };

    return classes[
      this.getProcessStatus(process)
    ];
  }

  canManageClasses(
    process: EnrollmentProcess,
  ): boolean {
    return (
      this.getProcessStatus(process) ===
      'NOT_STARTED'
    );
  }

  openCreateModal(): void {
    this.selectedProcess = null;

    this.isModalOpen = true;
  }

  openUpdateModal(): void {
    const selectedProcess =
      this.selectedProcess;

    if (
      selectedProcess &&
      this.canManageClasses(
        selectedProcess,
      )
    ) {
      this.isModalOpen = true;
    }
  }

  onModalSuccess(): void {
    this.isModalOpen = false;

    this.loadProcesses();
  }

  inactivateProcess(): void {
    const selectedProcess =
      this.selectedProcess;

    const processId =
      selectedProcess?.id;

    if (
      !selectedProcess ||
      !processId ||
      !this.canManageClasses(
        selectedProcess,
      )
    ) {
      return;
    }

    const confirmed =
      confirm(
        `Inativar "${selectedProcess.title}"?`,
      );

    if (!confirmed) {
      return;
    }

    this.service
      .update(
        processId,
        {
          ...selectedProcess,
          active: false,
        },
      )
      .subscribe({
        next: () => {
          this.loadProcesses();
        },

        error: error => {
          console.error(
            'Erro ao inativar o processo:',
            error,
          );
        },
      });
  }

  generatePDF(): void {
    const doc =
      new jsPDF(
        'p',
        'mm',
        'a4',
      );

    doc.text(
      'Relatório de Processos',
      14,
      15,
    );

    autoTable(doc, {
      startY: 20,

      head: [[
        'Título',
        'Período',
        'Início',
        'Fim',
        'Situação',
      ]],

      body:
        this.filteredProcesses.map(
          process => [
            process.title,

            this.getPeriodInfo(
              process.academicPeriodId,
            ),

            this.datePipe.transform(
              process.startDate,
              'dd/MM/yyyy HH:mm',
            ),

            this.datePipe.transform(
              process.endDate,
              'dd/MM/yyyy HH:mm',
            ),

            this.getStatusLabel(process),
          ],
        ),
    });

    doc.save('processos.pdf');
  }

  generateLinkedClassesPDF(): void {
    const selectedProcess =
      this.selectedProcess;

    if (
      !selectedProcess ||
      this.linkedClasses.length === 0
    ) {
      return;
    }

    const doc =
      new jsPDF();

    doc.setFontSize(16);

    doc.text(
      `Turmas vinculadas: ${selectedProcess.title}`,
      14,
      15,
    );

    autoTable(doc, {
      startY: 20,

      head: [[
        'Turma',
      ]],

      body:
        this.linkedClasses
          .map(
            link =>
              link.classGroupId,
          )
          .filter(
            (
              id,
            ): id is string =>
              typeof id === 'string',
          )
          .map(classGroupId => [
            this.getClassGroupName(
              classGroupId,
            ),
          ]),

      theme: 'striped',

      headStyles: {
        fillColor: [
          35,
          32,
          102,
        ],
      },
    });

    doc.save(
      `turmas-${selectedProcess.title.replace(
        /\s+/g,
        '-',
      )}.pdf`,
    );
  }
}