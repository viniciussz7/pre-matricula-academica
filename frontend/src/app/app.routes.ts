import { Routes } from '@angular/router';

import { AuthGuard } from './core/guards/auth.guard';

import { AuthLayoutComponent } from './layout/auth-layout/auth-layout.component';
import { AdminLayoutComponent } from './layout/admin-layout/admin-layout.component';
import { StudentLayoutComponent } from './layout/student-layout/student-layout.component';

export const routes: Routes = [
  // 1. ÁREA PÚBLICA
  {
    path: 'auth',
    component: AuthLayoutComponent,

    children: [
      {
        path: 'login',

        loadComponent: () =>
          import(
            './features/auth/pages/login/login.component'
          ).then(
            module =>
              module.LoginComponent,
          ),
      },

      {
        path: 'first-access/request',

        loadComponent: () =>
          import(
            './features/auth/pages/first-access/request/first-access-request.component'
          ).then(
            module =>
              module.FirstAccessRequestComponent,
          ),
      },

      {
        path: 'first-access/confirm',

        loadComponent: () =>
          import(
            './features/auth/pages/first-access/confirm/first-access-confirm.component'
          ).then(
            module =>
              module.FirstAccessConfirmComponent,
          ),
      },

      {
        path: '',
        redirectTo: 'login',
        pathMatch: 'full',
      },
    ],
  },

  // 2. ÁREA DO ADMINISTRADOR
  {
    path: 'admin',
    component: AdminLayoutComponent,

    canActivate: [
      AuthGuard,
    ],

    data: {
      expectedRole: 'ADMIN',
    },

    children: [
      {
        path: 'dashboard',

        loadComponent: () =>
          import(
            './features/admin/dashboard/pages/dashboard/dashboard.component'
          ).then(
            module =>
              module.DashboardComponent,
          ),
      },

      {
        path: 'student',

        loadComponent: () =>
          import(
            './features/admin/student/pages/student/student.component'
          ).then(
            module =>
              module.StudentsComponent,
          ),
      },

      // GERENCIAMENTO DE ADMINISTRADORES
      {
        path: 'administrator',

        loadComponent: () =>
          import(
            './features/admin/admin/pages/admin/admin.component'
          ).then(
            module =>
              module.AdminComponent,
          ),
      },

      {
        path: 'discipline',

        loadComponent: () =>
          import(
            './features/admin/discipline/pages/discipline/discipline.component'
          ).then(
            module =>
              module.DisciplineComponent,
          ),
      },

      {
        path: 'academic-period',

        loadComponent: () =>
          import(
            './features/admin/academicperiod/pages/academic-period/academic-period.component'
          ).then(
            module =>
              module.AcademicPeriodComponent,
          ),
      },

      {
        path: 'class-group',

        loadComponent: () =>
          import(
            './features/admin/classgroup/pages/class-group/class-group.component'
          ).then(
            module =>
              module.ClassGroupComponent,
          ),
      },

      {
        path: 'enrollment-process',

        loadComponent: () =>
          import(
            './features/admin/enrollmentprocess/pages/enrollment-process/enrollment-process.component'
          ).then(
            module =>
              module.EnrollmentProcessComponent,
          ),
      },

      {
        path: 'report',

        loadComponent: () =>
          import(
            './features/admin/reports/pages/reports/reports.component'
          ).then(
            module =>
              module.ReportsComponent,
          ),
      },

      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full',
      },
    ],
  },

  // 3. ÁREA DO ALUNO
  {
    path: 'student',
    component: StudentLayoutComponent,

    canActivate: [
      AuthGuard,
    ],

    data: {
      expectedRole: 'STUDENT',
    },

    children: [
      {
        path: 'dashboard',

        loadComponent: () =>
          import(
            './features/student/dashboard/dashboard.component'
          ).then(
            module =>
              module.DashboardComponent,
          ),
      },

      {
        path: 'enrollment',

        loadComponent: () =>
          import(
            './features/student/enrollment/pages/enrollment.component'
          ).then(
            module =>
              module.EnrollmentComponent,
          ),
      },

      {
        path: 'my-history',

        loadComponent: () =>
          import(
            './features/student/myhistory/pages/my-history.component'
          ).then(
            module =>
              module.MyHistoryComponent,
          ),
      },

      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full',
      },
    ],
  },

  // Rota padrão
  {
    path: '',
    redirectTo: 'auth/login',
    pathMatch: 'full',
  },

  {
    path: '**',
    redirectTo: 'auth/login',
  },
];