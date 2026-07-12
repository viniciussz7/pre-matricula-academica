import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { AuthLayoutComponent } from './layout/auth-layout/auth-layout.component';
import { AdminLayoutComponent } from './layout/admin-layout/admin-layout.component';
import { StudentLayoutComponent } from './layout/student-layout/student-layout.component';

export const routes: Routes = [
  // 1. ÁREA PÚBLICA (Login e Primeiro Acesso)
  {
    path: 'auth',
    component: AuthLayoutComponent,
    children: [
      { 
        path: 'login', 
        loadComponent: () => import('./features/auth/pages/login/login.component').then(m => m.LoginComponent) 
      },
      { 
        path: 'first-access/request', 
        loadComponent: () => import('./features/auth/pages/first-access/request/first-access-request.component').then(m => m.FirstAccessRequestComponent) 
      },
      { 
        path: 'first-access/confirm', 
        loadComponent: () => import('./features/auth/pages/first-access/confirm/first-access-confirm.component').then(m => m.FirstAccessConfirmComponent) 
      },
      { path: '', redirectTo: 'login', pathMatch: 'full' }
    ]
  },

  // 2. ÁREA DO ADMIN (Protegida)
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [AuthGuard],
    data: { expectedRole: 'ADMIN' },
    children: [
      { 
        path: 'dashboard', 
        loadComponent: () => import('./features/admin/dashboard/pages/dashboard/dashboard.component').then(m => m.DashboardComponent) 
      },
      { 
        path: 'student', 
        loadComponent: () => import('./features/admin/student/pages/student/student.component').then(m => m.StudentsComponent) 
      },
      { 
        path: 'discipline', 
        loadComponent: () => import('./features/admin/discipline/pages/discipline/discipline.component').then(m => m.DisciplineComponent) 
      },
      { 
        path: 'academic-period', 
        loadComponent: () => import('./features/admin/academicperiod/pages/academic-period/academic-period.component').then(m => m.AcademicPeriodComponent) 
      },
      { 
        path: 'class-group', 
        loadComponent: () => import('./features/admin/classgroup/pages/class-group/class-group.component').then(m => m.ClassGroupComponent) 
      },
      { 
        path: 'enrollment-process', 
        loadComponent: () => import('./features/admin/enrollmentprocess/pages/enrollment-process/enrollment-process.component').then(m => m.EnrollmentProcessComponent) 
      },
      { 
        path: 'report', 
        loadComponent: () => import('./features/admin/reports/pages/reports/reports.component').then(m => m.ReportsComponent) 
      },
      // Redirecionamento padrão (Apenas um!)
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },

  // 3. ÁREA DO ALUNO (Protegida)
  {
    path: 'student',
    component: StudentLayoutComponent,
    canActivate: [AuthGuard],
    data: { expectedRole: 'STUDENT' }, // O Guard vai checar se o role é STUDENT
    children: [
      { 
        path: 'dashboard', 
        loadComponent: () => import('./features/student/dashboard/dashboard.component').then(m => m.DashboardComponent) 
      },
      { 
        path: 'enrollment-process', 
        loadComponent: () => import('./features/student/enrollmentprocess/pages/enrollment-process.component').then(m => m.EnrollmentProcessComponent) 
      },
      { 
        path: 'my-history', 
        loadComponent: () => import('./features/student/myhistory/pages/my-history.component').then(m => m.MyHistoryComponent) 
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },

  // Rota padrão cai no login
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
  { path: '**', redirectTo: 'auth/login' }
];