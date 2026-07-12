import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-sidebar', 
  standalone: true, 
  imports: [CommonModule, RouterModule], 
  templateUrl: './sidebar.html',
})
export class SidebarComponent implements OnInit {
  fullName: string = '';
  firstName: string = '';

  constructor(
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() { this.loadAmdins(); }

  loadAmdins() {
    this.fullName = localStorage.getItem('fullName') || 'Aluno'; 
    this.firstName = this.fullName.split(' ')[0];
  }
}