import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DisciplineService } from '../../services/discipline.service';
import { Discipline } from '../../models/discipline.model';
import { DisciplineModalComponent } from '../../components/discipline-modal/discipline-modal.component';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-discipline',
  standalone: true,
  imports: [CommonModule, FormsModule, DisciplineModalComponent],
  templateUrl: './discipline.html'
})
export class DisciplineComponent implements OnInit {
  disciplines: Discipline[] = [];
  filteredDisciplines: Discipline[] = [];
  searchTerm: string = '';
  
  selectedDiscipline: Discipline | null = null;
  isModalOpen = false;

  constructor(private service: DisciplineService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() { this.loadDisciplines(); }

  loadDisciplines() {
    this.service.getAll().subscribe(data => {
      this.disciplines = data.sort((a, b) => {
        if (a.active === b.active) {
          return a.name.localeCompare(b.name); 
        }
        return a.active ? -1 : 1;
      });

      this.selectedDiscipline = null;
      this.onSearch();

      this.cdr.detectChanges();
    });
  }

  onSearch() {
    const term = this.searchTerm.toLowerCase();
    this.filteredDisciplines = this.disciplines.filter(d => 
      d.name.toLowerCase().includes(term) || 
      d.code.toLowerCase().includes(term)
    );
  }

  selectDiscipline(discipline: Discipline) {
    this.selectedDiscipline = this.selectedDiscipline?.id === discipline.id ? null : discipline;
  }

  openCreateModal() {
    this.selectedDiscipline = null; 
    this.isModalOpen = true;
  }

  openUpdateModal() {
    if (this.selectedDiscipline) {
      this.isModalOpen = true; 
    }
  }

  inactivateDiscipline() {
    if (this.selectedDiscipline?.id) {
      if (confirm(`Deseja inativar a disciplina ${this.selectedDiscipline.code}?`)) {
        this.service.delete(this.selectedDiscipline.id).subscribe(() => this.loadDisciplines());
      }
    }
  }

  generatePDF() {
    const doc = new jsPDF();
    doc.setFontSize(16);
    doc.text('Relatório de Disciplinas', 14, 15);
    
    const tableData = this.disciplines.map(d => [
      d.code, 
      d.name, 
      `${d.workload}h`, 
      d.prerequisites || 'Nenhum', 
      d.active ? 'Ativa' : 'Inativa'
    ]);

    autoTable(doc, {
      startY: 20,
      head: [['Código', 'Nome', 'CH', 'Pré-requisitos', 'Status']],
      body: tableData,
      theme: 'grid',
      headStyles: { fillColor: [35, 32, 102] }
    });

    doc.save('lista-disciplinas.pdf');
  }

  onModalSuccess() {
    this.isModalOpen = false;
    this.loadDisciplines();
  }
}