import {
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { ClassGroupService } from '../../services/class-group.service';
import { DisciplineService } from '../../../discipline/services/discipline.service';
import { AcademicPeriodService } from '../../../academicperiod/services/academic-period.service';

import { ClassGroup } from '../../models/class-group.model';
import { ClassGroupModalComponent } from '../../components/classgroup-modal/class-group-modal.component';

import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

interface AcademicPeriodGroup {
  id: string;
  code: string;
  name: string;
  active: boolean;
  classGroups: ClassGroup[];
}

@Component({
  selector: 'app-class-group',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ClassGroupModalComponent,
  ],
  templateUrl: './class-group.html',
})
export class ClassGroupComponent implements OnInit {
  classGroups: ClassGroup[] = [];
  filteredClassGroups: ClassGroup[] = [];

  groupedClassGroups: AcademicPeriodGroup[] = [];

  searchTerm = '';

  selectedClassGroup: ClassGroup | null = null;
  isModalOpen = false;

  disciplines: any[] = [];
  periods: any[] = [];

  expandedPeriodIds = new Set<string>();

  isLoading = false;
  loadErrorMessage: string | null = null;

  constructor(
    private readonly service: ClassGroupService,
    private readonly disciplineService: DisciplineService,
    private readonly periodService: AcademicPeriodService,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadData();
  }

  /**
   * Carrega disciplinas, períodos e turmas em paralelo.
   * A lista só é agrupada depois que todos os dados chegam.
   */
  loadData(): void {
    this.isLoading = true;
    this.loadErrorMessage = null;

    forkJoin({
      disciplines: this.disciplineService.getAll(),
      periods: this.periodService.getAll(),
      classGroups: this.service.getAll(),
    }).subscribe({
      next: ({ disciplines, periods, classGroups }) => {
        this.disciplines = disciplines;
        this.periods = periods;

        this.classGroups = classGroups.sort((a, b) => {
          if (a.active === b.active) {
            return a.name.localeCompare(
              b.name,
              'pt-BR',
              { sensitivity: 'base' },
            );
          }

          return a.active ? -1 : 1;
        });

        this.selectedClassGroup = null;

        this.onSearch();
        this.expandMostRecentPeriod();

        this.isLoading = false;
        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error(
          'Erro ao carregar os dados das turmas:',
          error,
        );

        this.loadErrorMessage =
          'Não foi possível carregar as turmas. Tente novamente.';

        this.isLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  /**
   * Mantido para ser usado depois de criar, editar ou inativar.
   */
  loadClassGroups(): void {
    this.service.getAll().subscribe({
      next: (data) => {
        this.classGroups = data.sort((a, b) => {
          if (a.active === b.active) {
            return a.name.localeCompare(
              b.name,
              'pt-BR',
              { sensitivity: 'base' },
            );
          }

          return a.active ? -1 : 1;
        });

        this.selectedClassGroup = null;

        this.onSearch();
        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erro ao buscar turmas:', error);

        this.loadErrorMessage =
          'Não foi possível atualizar a lista de turmas.';

        this.cdr.detectChanges();
      },
    });
  }

  getDisciplineInfo(id: string): string {
    const discipline = this.disciplines.find(
      item => item.id === id,
    );

    return discipline
      ? `${discipline.code} - ${discipline.name}`
      : 'Disciplina não encontrada';
  }

  getPeriodInfo(id: string): string {
    const period = this.periods.find(
      item => item.id === id,
    );

    return period
      ? period.code
      : 'Período não encontrado';
  }

  onSearch(): void {
    const term = this.searchTerm
      .trim()
      .toLocaleLowerCase('pt-BR');

    this.filteredClassGroups =
      this.classGroups.filter(classGroup => {
        const disciplineText =
          this.getDisciplineInfo(
            classGroup.disciplineId,
          ).toLocaleLowerCase('pt-BR');

        const periodText =
          this.getPeriodInfo(
            classGroup.academicPeriodId,
          ).toLocaleLowerCase('pt-BR');

        return (
          classGroup.name
            .toLocaleLowerCase('pt-BR')
            .includes(term) ||
          classGroup.code
            .toLocaleLowerCase('pt-BR')
            .includes(term) ||
          disciplineText.includes(term) ||
          periodText.includes(term)
        );
      });

    this.buildPeriodGroups();

    /*
     * Durante uma busca, abre automaticamente os períodos
     * que possuem resultados.
     */
    if (term) {
      this.groupedClassGroups.forEach(group => {
        this.expandedPeriodIds.add(group.id);
      });
    }
  }

  private buildPeriodGroups(): void {
    const groups = new Map<
      string,
      AcademicPeriodGroup
    >();

    for (
      const classGroup of this.filteredClassGroups
    ) {
      const period = this.periods.find(
        item =>
          item.id === classGroup.academicPeriodId,
      );

      const periodId =
        period?.id ??
        classGroup.academicPeriodId;

      if (!groups.has(periodId)) {
        groups.set(periodId, {
          id: periodId,
          code: period?.code ?? 'Sem período',
          name:
            period?.name ??
            'Período letivo não encontrado',
          active: period?.active ?? false,
          classGroups: [],
        });
      }

      groups
        .get(periodId)
        ?.classGroups.push(classGroup);
    }

    this.groupedClassGroups = Array
      .from(groups.values())
      .sort((a, b) =>
        b.code.localeCompare(
          a.code,
          'pt-BR',
          {
            numeric: true,
            sensitivity: 'base',
          },
        ),
      );
  }

  private expandMostRecentPeriod(): void {
    if (
      this.expandedPeriodIds.size > 0 ||
      this.groupedClassGroups.length === 0
    ) {
      return;
    }

    this.expandedPeriodIds.add(
      this.groupedClassGroups[0].id,
    );
  }

  togglePeriod(periodId: string): void {
    if (this.expandedPeriodIds.has(periodId)) {
      this.expandedPeriodIds.delete(periodId);
    } else {
      this.expandedPeriodIds.add(periodId);
    }
  }

  isPeriodExpanded(periodId: string): boolean {
    return this.expandedPeriodIds.has(periodId);
  }

  selectClassGroup(
    classGroup: ClassGroup,
  ): void {
    this.selectedClassGroup =
      this.selectedClassGroup?.id === classGroup.id
        ? null
        : classGroup;
  }

  openCreateModal(): void {
    this.selectedClassGroup = null;
    this.isModalOpen = true;
  }

  openUpdateModal(): void {
    if (this.selectedClassGroup) {
      this.isModalOpen = true;
    }
  }

  inactivateClassGroup(): void {
    if (!this.selectedClassGroup?.id) {
      return;
    }

    const confirmed = confirm(
      `Deseja inativar a turma ${this.selectedClassGroup.code}?`,
    );

    if (!confirmed) {
      return;
    }

    this.service
      .delete(this.selectedClassGroup.id)
      .subscribe({
        next: () => {
          this.loadClassGroups();
        },

        error: error => {
          console.error(
            'Erro ao inativar turma:',
            error,
          );

          this.loadErrorMessage =
            'Não foi possível inativar a turma.';

          this.cdr.detectChanges();
        },
      });
  }

  generatePDF(): void {
    const doc = new jsPDF(
      'l',
      'mm',
      'a4',
    );

    doc.setFontSize(16);
    doc.text(
      'Relatório de Turmas',
      14,
      15,
    );

    const tableData =
      this.filteredClassGroups.map(
        classGroup => [
          classGroup.code,
          classGroup.name,
          this.getDisciplineInfo(
            classGroup.disciplineId,
          ),
          this.getPeriodInfo(
            classGroup.academicPeriodId,
          ),
          classGroup.vacancies,
          classGroup.allowOversubscription
            ? 'Sim'
            : 'Não',
          classGroup.active
            ? 'Ativo'
            : 'Inativo',
        ],
      );

    autoTable(doc, {
      startY: 20,

      head: [[
        'Código',
        'Nome',
        'Disciplina',
        'Período',
        'Vagas',
        'Permite ultrapassar?',
        'Status',
      ]],

      body: tableData,

      theme: 'grid',

      headStyles: {
        fillColor: [35, 32, 102],
      },
    });

    doc.save('lista-turmas.pdf');
  }

  onModalSuccess(): void {
    this.isModalOpen = false;
    this.loadClassGroups();
  }
}