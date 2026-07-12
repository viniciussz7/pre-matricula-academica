import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const isLoggedIn = this.authService.isLoggedIn();
    const role = this.authService.getUserRole();
    const expectedRole = route.data['expectedRole'];

    // Debug: Se não logar, veja isso no console
    if (!isLoggedIn) {
      console.log('Guard bloqueou: Usuário não logado');
      this.router.navigate(['/auth/login']);
      return false;
    }

    if (expectedRole && role !== expectedRole) {
      console.log('Guard bloqueou: Role insuficiente. Esperado:', expectedRole, 'Recebido:', role);
      this.router.navigate(['/auth/login']);
      return false;
    }

    return true;
  }
}