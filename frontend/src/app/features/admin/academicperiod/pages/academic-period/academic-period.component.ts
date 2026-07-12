import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AcademicPeriodService } from '../../services/academic-period.service';
import { AcademicPeriod } from '../../models/academic-period.model';
import { AcademicPeriodModalComponent } from '../../components/academicperiod-modal/academic-period-modal.component';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-academic-period',
  standalone: true,
  imports: [CommonModule, FormsModule, AcademicPeriodModalComponent, DatePipe],
  templateUrl: './academic-period.html'
})
export class AcademicPeriodComponent implements OnInit {
  academicPeriods: AcademicPeriod[] = [];
  filteredAcademicPeriods: AcademicPeriod[] = [];
  searchTerm: string = '';
  
  selectedAcademicPeriod: AcademicPeriod | null = null;
  isModalOpen = false;

  constructor(private service: AcademicPeriodService, private cdr: ChangeDetectorRef) {}

  ngOnInit() { this.loadPeriods(); }

  loadPeriods() {
    this.service.getAll().subscribe(data => {
      this.academicPeriods = data.sort((a, b) => {
        if (a.active === b.active){
          return a.name.localeCompare(b.name);
        }
        return a.active ? -1 : 1;
      });

      this.selectedAcademicPeriod = null;
      this.onSearch();

      this.cdr.detectChanges();
    });
  }

  onSearch() {
    const term = this.searchTerm.toLowerCase();
    this.filteredAcademicPeriods = this.academicPeriods.filter(p => 
      p.name.toLowerCase().includes(term) ||
      p.code.toLowerCase().includes(term)
    );
  }

  selectAcademicPeriod(p: AcademicPeriod) {
    this.selectedAcademicPeriod = this.selectedAcademicPeriod?.id === p.id ? null : p;
  }

  openCreateModal() {
    this.selectedAcademicPeriod = null; 
    this.isModalOpen = true;
  }

  openUpdateModal() {
    if (this.selectedAcademicPeriod) {
      this.isModalOpen = true; 
    }
  }

  inactivateAcademicPeriod() {
    if (this.selectedAcademicPeriod?.id){
      if(confirm(`Deseja inativar o período letivo ${this.selectedAcademicPeriod.code}?`)){
      this.service.delete(this.selectedAcademicPeriod.id).subscribe(() => this.loadPeriods());
      }
    } 
  }
  
  generatePDF() {
      const doc = new jsPDF();
      doc.setFontSize(16);
      doc.text('Relatório de Períodos Letivos', 14, 15);
      
      const tableData = this.academicPeriods.map(d => [
        d.code, 
        d.name,
        d.startDate,
        d.endDate, 
        d.active ? 'Ativo' : 'Inativo'
      ]);

      autoTable(doc, {
        startY: 20,
        head: [['Código', 'Nome', 'Início', 'Fim', 'Status']],
        body: tableData,
        theme: 'grid',
        headStyles: { fillColor: [35, 32, 102] }
      });

      doc.save('lista-períodos-letivos.pdf');
    }

  onModalSuccess() {
    this.isModalOpen = false;
    this.loadPeriods();
  }
}