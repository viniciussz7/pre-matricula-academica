import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NgIconComponent } from '@ng-icons/core';

interface DashboardAction {
  title: string;
  description: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    NgIconComponent,
  ],
  templateUrl: 'dashboard.html',
})
export class DashboardComponent {
  actions: DashboardAction[] = [
    {
    title: 'Gerenciar Alunos',
    description:
      'Cadastre, consulte e gerencie os alunos do sistema.',
    route: '/admin/student',
    icon: 'heroUserGroup',
  },
  {
    title: 'Gerenciar Administradores',
    description:
      'Cadastre e controle os usuários administrativos.',
    route: '/admin/administrator',
    icon: 'heroUserCircle',
  },
    {
      title: 'Gerenciar Turmas',
      description:
        'Cadastre turmas, defina vagas e associe disciplinas e períodos.',
      route: '/admin/class-group',
      icon: 'heroAcademicCap',
    },
    {
      title: 'Gerenciar Processos de Pré-Matrícula',
      description:
        'Crie processos, defina datas e escolha as turmas participantes.',
      route: '/admin/enrollment-process',
      icon: 'heroClipboardDocumentCheck',
    },
    {
      title: 'Visualizar Relatórios',
      description:
        'Acompanhe demanda, inscrições e indicadores dos processos.',
      route: '/admin/report',
      icon: 'heroChartBar',
    },
  ];
}