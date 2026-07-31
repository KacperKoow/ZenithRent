import { Routes } from '@angular/router';
import { authGuard,roleGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
  },
  {
    path: 'admin',
    loadComponent: () =>
      import('./features/admin-panel/admin-panel').then((m) => m.AdminPanel),
    canActivate: [roleGuard(['ADMIN'])], 
  },
  {
    path: 'employee',
    loadComponent: () =>
      import('./features/employee-panel/employee-panel').then((m) => m.EmployeePanel),
    canActivate: [roleGuard(['ADMIN', 'EMPLOYEE'])], 
  },
  {
    path: 'user',
    loadComponent: () => import('./features/user-panel/user-panel').then((m) => m.UserPanel),
    canActivate: [authGuard], 
  },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' },
];
