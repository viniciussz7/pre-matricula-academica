import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../../shared/components/sidebar/student/sidebar.component';

@Component({
  selector: 'app-student-layout',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent],
  templateUrl: './student-layout.html'
})
export class StudentLayoutComponent {}