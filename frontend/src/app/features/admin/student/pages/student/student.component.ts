import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StudentService } from '../../services/student.service';
import { Student } from '../../models/student.model';
import { StudentModalComponent } from '../../components/student-modal/student-modal.component';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-student',
  standalone: true,
  imports: [CommonModule, FormsModule, StudentModalComponent],
  templateUrl: './student.html'
})
export class StudentsComponent implements OnInit {
  students: Student[] = [];
  filteredStudents: Student[] = [];
  searchTerm: string = '';
  
  selectedStudent: Student | null = null;
  isModalOpen = false;

  constructor(private service: StudentService) {}

  ngOnInit() { this.loadStudents(); }

  loadStudents() {
    this.service.getAll().subscribe(data => {
      this.students = data.sort((a, b) => {
        if (a.active === b.active){
          return a.fullName.localeCompare(b.fullName);
        }
        return a.active ? -1 : 1;
      });

      this.selectedStudent = null;
      this.onSearch();
    });
  }

  onSearch() {
      const term = this.searchTerm.toLowerCase();
      this.filteredStudents = this.students.filter(p => 
        p.fullName.toLowerCase().includes(term) ||
        p.registrationNumber.toLowerCase().includes(term)
      );
    }
  
    selectStudent(p: Student) {
      this.selectedStudent = this.selectedStudent?.id === p.id ? null : p;
    }
  
    openCreateModal() {
      this.selectedStudent = null; 
      this.isModalOpen = true;
    }
  
    openUpdateModal() {
      if (this.selectedStudent) {
        this.isModalOpen = true; 
      }
    }

    inactivateStudent() {
    if (this.selectedStudent?.id){
      if(confirm(`Deseja inativar o aluno ${this.selectedStudent.fullName}?`)){
      this.service.delete(this.selectedStudent.id).subscribe(() => this.loadStudents());
      }
    } 
  }

  generatePDF() {
        const doc = new jsPDF();
        doc.setFontSize(16);
        doc.text('Relatório de Períodos Letivos', 14, 15);
        
        const tableData = this.students.map(d => [
          d.fullName,
          d.email,
          d.registrationNumber, 
          d.active ? 'Ativo' : 'Inativo'
        ]);
  
        autoTable(doc, {
          startY: 20,
          head: [['Nome', 'Email', 'Nº de Matrícula', 'Status']],
          body: tableData,
          theme: 'grid',
          headStyles: { fillColor: [35, 32, 102] }
        });
  
        doc.save('lista-alunos.pdf');
      }
  
    onModalSuccess() {
      this.isModalOpen = false;
      this.loadStudents();
    }

}