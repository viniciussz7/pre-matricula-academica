import { Routes } from '@angular/router';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./auth/pages/login/login.component').then(m => m.LoginComponent) },
  
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: 'dashboard', loadComponent: () => import('./features/dashboard/pages/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'students', loadComponent: () => import('./features/student/pages/student/student.component').then(m => m.StudentsComponent) },
      { path: 'disciplines', loadComponent: () => import('./features/discipline/pages/discipline/discipline.component').then(m => m.DisciplineComponent) },
      { path: 'academic-period', loadComponent: () => import('./features/academicperiod/pages/academic-period/academic-period.component').then(m => m.AcademicPeriodComponent) },
      { path: 'class-group', loadComponent: () => import('./features/classgroup/pages/class-group/class-group.component').then(m => m.ClassGroupComponent) },
      { path: 'enrollment-process', loadComponent: () => import('./features/enrollmentprocess/pages/enrollment-process/enrollment-process.component').then(m => m.EnrollmentProcessComponent) },
      { path: 'reports', loadComponent: () => import('./features/reports/pages/reports/reports.component').then(m => m.ReportsComponent) },
      
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  }
];