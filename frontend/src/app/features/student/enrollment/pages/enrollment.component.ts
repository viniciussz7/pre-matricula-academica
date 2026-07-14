import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { firstValueFrom, forkJoin } from 'rxjs';

// Serviços
import { EnrollmentService } from '../services/enrollment.service';
import { EnrollmentProcessService } from '../../../admin/enrollmentprocess/services/enrollment-process.service';
import { EnrollmentProcessClassService } from '../../../admin/enrollmentprocess/services/enrollment-process-class.service';
import { ClassGroupService } from '../../../admin/classgroup/services/class-group.service';
import { AcademicPeriodService } from '../../../admin/academicperiod/services/academic-period.service';
import { DisciplineService } from '../../../admin/discipline/services/discipline.service';

@Component({
  selector: 'app-enrollment',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './enrollment.html'
})
export class EnrollmentComponent implements OnInit {
  view: 'LIST_PROCESS' | 'LIST_CLASSES' | 'ENROLLED' = 'LIST_PROCESS';

  processes: any[] = [];
  classes: any[] = [];
  selectedProcess: any = null;
  selectedClassIds: string[] = [];
  allDisciplines: any[] = [];
  allPeriods: any[] = [];
  isLoading = false;

  constructor(
    private enrollmentService: EnrollmentService,
    private processService: EnrollmentProcessService,
    private classService: EnrollmentProcessClassService,
    private classGroupService: ClassGroupService,
    private academicPeriodService: AcademicPeriodService,
    private disciplineService: DisciplineService,
    private cdr: ChangeDetectorRef 
  ) { }

  async ngOnInit() {
    this.isLoading = true;
    this.disciplineService.getAll().subscribe(data => this.allDisciplines = data);
    this.academicPeriodService.getAll().subscribe(data => this.allPeriods = data);

    await this.loadProcesses();
    this.isLoading = false;
    this.cdr.detectChanges();
  }

  async loadProcesses() {
    try {
      const allProcesses = await firstValueFrom(this.processService.findAll());
      this.processes = allProcesses.filter(p => p.active);
    } catch (e) {
      console.error("Erro ao carregar processos:", e);
    }
  }

  async selectProcess(process: any) {
    this.isLoading = true;
    this.selectedProcess = process;
    try {
      const listaSimples = await firstValueFrom(this.classService.findByProcess(process.id));

      const chamadas = listaSimples.map(item =>
        this.classGroupService.getById(item.classGroupId)
      );

      if (chamadas.length > 0) {
        const detalhesDasTurmas = await firstValueFrom(forkJoin(chamadas));

        this.classes = listaSimples.map((item, index) => ({
          ...item,
          detalhes: detalhesDasTurmas[index]
        }));
      } else {
        this.classes = [];
      }

      this.view = 'LIST_CLASSES';
    } catch (e) {
      console.error(e);
    } finally {
      this.isLoading = false;
      this.cdr.detectChanges();
    }
  }

  toggleClass(classId: string) {
    const index = this.selectedClassIds.indexOf(classId);
    if (index > -1) {
      this.selectedClassIds.splice(index, 1);
    } else {
      this.selectedClassIds.push(classId);
    }
  }

  getDisciplineName(id: string): string {
    const d = this.allDisciplines.find(item => item.id === id);
    return d ? d.name : 'Carregando...';
  }

  getPeriodName(id: string): string {
    const p = this.allPeriods.find(item => item.id === id);
    return p ? p.name : 'Carregando...';
  }

  async saveEnrollment() {
  if (this.selectedClassIds.length === 0) return alert("Selecione ao menos uma turma.");

  this.isLoading = true;
  const dto = { 
    enrollmentProcessId: this.selectedProcess.id, 
    enrollmentProcessClassIds: this.selectedClassIds 
  };

  try {
    await firstValueFrom(this.enrollmentService.create(dto));
    alert("Matrícula realizada com sucesso!");
    this.view = 'ENROLLED';
  } catch (e: any) {
    alert("Erro: " + (e.error?.message || "Verifique as datas do processo."));
  } finally {
    this.isLoading = false;
    this.cdr.detectChanges();
  }
}
}