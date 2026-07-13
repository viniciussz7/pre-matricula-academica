import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { NgIconComponent } from '@ng-icons/core';
import { StudentService } from '../../../student/services/student.service';
import { DisciplineService } from '../../../discipline/services/discipline.service';
import { AcademicPeriodService } from '../../../academicperiod/services/academic-period.service';
import { ClassGroupService } from '../../../classgroup/services/class-group.service';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink, CommonModule, NgIconComponent],
  templateUrl: './dashboard.html'
})
export class DashboardComponent implements OnInit {

  // Estrutura de dados para os Cards
  stats = [
    { title: 'Total de Alunos', value: '0', icon: 'heroUserGroup', color: 'bg-blue-500' },
    { title: 'Disciplinas Ativas', value: '0', icon: 'heroBookOpen', color: 'bg-violet-500' },
    { title: 'Períodos Ativos', value: '0', icon: 'heroCalendarDays', color: 'bg-indigo-500' },
    { title: 'Turmas Ativas', value: '0', icon: 'heroAcademicCap', color: 'bg-rose-500' },
    { title: 'Pré-Matrículas Ativas', value: '0', icon: 'heroClock', color: 'bg-amber-500' },
  ];

  recentActivities: any[] = [];

  constructor(
    private studentService: StudentService,
    private disciplineService: DisciplineService,
    private periodService: AcademicPeriodService,
    private classService: ClassGroupService,
    private cdr: ChangeDetectorRef,
  ) { }

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    // forkJoin dispara todas as chamadas simultaneamente
    forkJoin({
      students: this.studentService.getAll(),
      disciplines: this.disciplineService.getAll(),
      periods: this.periodService.getAll(),
      classes: this.classService.getAll(),
    }).subscribe({
      next: (data) => {
        // Atribuição segura com fallback (|| []) caso o retorno seja nulo
        this.stats[0].value = (data.students || []).filter(item => item.active === true).length.toString();
        this.stats[1].value = (data.disciplines || []).filter(item => item.active === true).length.toString();
        this.stats[2].value = (data.periods || []).filter(item => item.active === true).length.toString();
        this.stats[3].value = (data.classes || []).filter(item => item.active === true).length.toString();

        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erro ao carregar dados do dashboard:', err);
        // Opcional: tratar erro na tela se necessário
      }
    });

  }
}