import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';


@Component({
  selector: 'app-sidebar', 
  standalone: true, 
  imports: [CommonModule, RouterModule], 
  templateUrl: './sidebar.html',
})
export class SidebarComponent implements OnInit {
  firstName: string = '';

  constructor(
    private authService: AuthService, 
    private router: Router
  ) {}

  ngOnInit() {
    const user = this.authService.getUserData();
    
    if (user && user.fullName) {
      this.firstName = user.fullName.split(' ')[0]; 
    } else {
      this.firstName = 'Usuário';
    }
  }

  logout(): void {
    this.authService.logout();
    
    this.router.navigate(['/auth/login']); 
  }
}