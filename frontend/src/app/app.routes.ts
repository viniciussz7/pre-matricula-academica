import { Routes } from '@angular/router';

export const routes: Routes = [
    // Rota padrão redireciona para o login
    { path: '', redirectTo: 'auth', pathMatch: 'full' },

    {
        path: 'auth',
        loadChildren: () => import('./features/auth/auth-module').then(m => m.AuthModule)
    },

    {
        path: 'student',
        loadChildren: () => import('./features/student/student-module').then(m => m.StudentModule)
    },

    {
        path: 'discipline',
        loadChildren: () => import('./features/discipline/discipline-module').then(m => m.DisciplineModule)
    },

    {
        path: 'classgroup',
        loadChildren: () => import('./features/classgroup/classgroup-module').then(m => m.ClassgroupModule)
    },

    {
        path: 'academicperiod',
        loadChildren: () => import('./features/academicperiod/academicperiod-module').then(m => m.AcademicperiodModule)
    },

    {
        path: 'enrollmentprocess',
        loadChildren: () => import('./features/enrollmentprocess/enrollmentprocess-module').then(m => m.EnrollmentprocessModule)
    },

    {
        path: 'enrollment',
        loadChildren: () => import('./features/enrollment/enrollment-module').then(m => m.EnrollmentModule)
    },

    {
        path: 'reports',
        loadChildren: () => import('./features/reports/reports-module').then(m => m.ReportModule)
    }

];