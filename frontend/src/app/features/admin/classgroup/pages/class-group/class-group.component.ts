import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClassGroupService } from '../../services/class-group.service';
import { DisciplineService } from '../../../discipline/services/discipline.service';
import { AcademicPeriodService } from '../../../academicperiod/services/academic-period.service';
import { ClassGroup } from '../../models/class-group.model';
import { ClassGroupModalComponent } from '../../components/classgroup-modal/class-group-modal.component';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-class-group',
  standalone: true,
  imports: [CommonModule, FormsModule, ClassGroupModalComponent],
  templateUrl: './class-group.html'
})
export class ClassGroupComponent implements OnInit {
  classGroups: ClassGroup[] = [];
  filteredClassGroups: ClassGroup[] = [];
  searchTerm: string = '';

  selectedClassGroup: ClassGroup | null = null;
  isModalOpen = false;

  disciplines: any[] = [];
  periods: any[] = [];

  constructor(
    private service: ClassGroupService,
    private disciplineService: DisciplineService,
    private periodService: AcademicPeriodService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit() {
    this.loadAuxiliaryData();
    this.loadClassGroups();
  }

  loadAuxiliaryData() {
    this.disciplineService.getAll().subscribe(data => this.disciplines = data);
    this.periodService.getAll().subscribe(data => this.periods = data);
    this.cdr.detectChanges();
  }

  // Função para buscar o texto da disciplina pelo ID
  getDisciplineInfo(id: string): string {
    const disc = this.disciplines.find(d => d.id === id);
    return disc ? `${disc.code} - ${disc.name}` : 'Carregando...';
  }

  // Função para buscar o texto do período pelo ID
  getPeriodInfo(id: string): string {
    const period = this.periods.find(p => p.id === id);
    return period ? period.code : 'Carregando...';
  }

  loadClassGroups() {
    this.service.getAll().subscribe(data => {
      this.classGroups = data.sort((a, b) => {
        if (a.active === b.active) {
          return a.name.localeCompare(b.name);
        }
        return a.active ? -1 : 1;
      });

      this.selectedClassGroup = null;
      this.onSearch();
      this.cdr.detectChanges();

    });
  }

  onSearch() {
    const term = this.searchTerm.toLowerCase();
    this.filteredClassGroups = this.classGroups.filter(p =>
      p.name.toLowerCase().includes(term) ||
      p.code.toLowerCase().includes(term)
    );
  }

  selectClassGroup(p: ClassGroup) {
    this.selectedClassGroup = this.selectedClassGroup?.id === p.id ? null : p;
  }

  openCreateModal() {
    this.selectedClassGroup = null;
    this.isModalOpen = true;
  }

  openUpdateModal() {
    if (this.selectedClassGroup) {
      this.isModalOpen = true;
    }
  }

  inactivateClassGroup() {
    if (this.selectedClassGroup?.id) {
      if (confirm(`Deseja inativar a turma ${this.selectedClassGroup.code}?`)) {
        this.service.delete(this.selectedClassGroup.id).subscribe(() => this.loadClassGroups());
      }
    }
  }

  generatePDF() {
    const doc = new jsPDF('l', 'mm', 'a4');

    doc.setFontSize(16);
    doc.text('Relatório de Turmas', 14, 15);

    const tableData = this.filteredClassGroups.map(d => [
      d.code,
      d.name,
      this.getDisciplineInfo(d.disciplineId),
      this.getPeriodInfo(d.academicPeriodId),
      d.vacancies,
      d.allowOversubscription ? 'Sim' : 'Não',
      d.active ? 'Ativo' : 'Inativo'
    ]);

    autoTable(doc, {
      startY: 20,
      head: [['Código', 'Nome', 'Disciplina', 'Período', 'Vagas', 'Perimite Ultrapassar?', 'Status']],
      body: tableData,
      theme: 'grid',
      headStyles: { fillColor: [35, 32, 102] }
    });

    doc.save('lista-turmas.pdf');
  }

  onModalSuccess() {
    this.isModalOpen = false;
    this.loadClassGroups();
  }
}