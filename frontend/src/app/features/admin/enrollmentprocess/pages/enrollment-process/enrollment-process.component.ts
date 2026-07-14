import { ChangeDetectorRef, Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs'; // Importante para gerenciar requisições
import { EnrollmentProcessService } from '../../services/enrollment-process.service';
import { AcademicPeriodService } from '../../../academicperiod/services/academic-period.service';
import { EnrollmentProcess } from '../../models/enrollment-process.model';
import { EnrollmentProcessModalComponent } from '../../components/enrollment-process-modal/enrollment-process-modal.component';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { ClassGroupService } from '../../../classgroup/services/class-group.service';
import { EnrollmentProcessClassService } from '../../services/enrollment-process-class.service';
import { EnrollmentProcessClass } from '../../models/enrollment-process-class.model';

@Component({
  selector: 'app-enrollment-process',
  standalone: true,
  imports: [CommonModule, FormsModule, EnrollmentProcessModalComponent],
  providers: [DatePipe],
  templateUrl: './enrollment-process.html'
})
export class EnrollmentProcessComponent implements OnInit, OnDestroy {
  processes: EnrollmentProcess[] = [];
  filteredProcesses: EnrollmentProcess[] = [];
  searchTerm: string = '';
  periods: any[] = [];
  
  // Variáveis de estado
  allActiveClassGroups: any[] = [];
  availableClasses: any[] = [];
  linkedClasses: EnrollmentProcessClass[] = [];
  selectedClassGroupId: string = '';
  selectedProcess: EnrollmentProcess | null = null;
  isModalOpen = false;

  private linkedClassesSub?: Subscription; // Controle de requisição

  constructor(
    private service: EnrollmentProcessService,
    private periodService: AcademicPeriodService,
    private datePipe: DatePipe,
    private classGroupService: ClassGroupService,
    private cdr: ChangeDetectorRef,
    private enrollmentProcessClassService: EnrollmentProcessClassService
  ) { }

  ngOnInit() {
    this.loadAuxiliaryData();
    this.loadProcesses();
    this.loadActiveClassGroups();
  }

  ngOnDestroy() {
    this.linkedClassesSub?.unsubscribe(); // Limpeza ao sair do componente
  }

  loadAuxiliaryData() {
    this.periodService.getAll().subscribe(data => this.periods = data);
  }

  loadActiveClassGroups() {
    this.classGroupService.getAll().subscribe({
      next: (data) => this.allActiveClassGroups = data.filter(c => c.active === true)
      
    });
  }

  loadProcesses() {
    this.service.findAll().subscribe(data => {
      this.processes = data.sort((a, b) => a.active === b.active ? a.title.localeCompare(b.title) : (a.active ? -1 : 1));
      this.onSearch();
      this.cdr.detectChanges();
    });
  }

  onSearch() {
    const term = this.searchTerm.toLowerCase();
    this.filteredProcesses = this.processes.filter(p => p.title.toLowerCase().includes(term));
  }

  selectProcess(p: EnrollmentProcess) {
    // 1. Cancela qualquer busca anterior pendente
    this.linkedClassesSub?.unsubscribe();

    // 2. Reseta o estado imediatamente
    this.selectedProcess = p;
    this.linkedClasses = [];
    this.availableClasses = [];
    this.selectedClassGroupId = '';

    // 3. Carrega novos dados
    this.loadLinkedClasses();
  }

  loadLinkedClasses() {
    if (!this.selectedProcess?.id) return;

    this.linkedClassesSub = this.enrollmentProcessClassService.findByProcess(this.selectedProcess.id).subscribe({
      next: (data) => {
        this.linkedClasses = data;
        this.updateAvailableClasses();
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Erro ao buscar turmas vinculadas', err)
    });
  }

  updateAvailableClasses() {
    if (!this.selectedProcess) return;

    const linkedIds = this.linkedClasses.map(l => l.classGroupId);
    
    this.availableClasses = this.allActiveClassGroups.filter(c => 
      c.academicPeriodId === this.selectedProcess?.academicPeriodId && 
      !linkedIds.includes(c.id)
    );
  }

  linkClassToProcess() {
    if (!this.selectedProcess?.id || !this.selectedClassGroupId) return;

    const newLink: EnrollmentProcessClass = {
      enrollmentProcessId: this.selectedProcess.id,
      classGroupId: this.selectedClassGroupId,
      active: true
    };

    this.enrollmentProcessClassService.create(newLink).subscribe({
      next: () => {
        this.loadLinkedClasses(); // Recarrega e atualiza o filtro de disponíveis
        this.selectedClassGroupId = '';
      },
      error: (err) => alert('Erro: ' + (err.error?.message || 'Erro ao vincular'))
    });
  }

  getClassGroupName(id: string): string {
    return this.allActiveClassGroups.find(c => c.id === id)?.name || 'Turma não encontrada';
  }

  getPeriodInfo(id: string): string {
    return this.periods.find(p => p.id === id)?.name || 'Carregando...';
  }

  openCreateModal() { this.selectedProcess = null; this.isModalOpen = true; }
  openUpdateModal() { if (this.selectedProcess) this.isModalOpen = true; }
  onModalSuccess() { this.isModalOpen = false; this.loadProcesses(); }

  inactivateProcess() {
    if (this.selectedProcess?.id && confirm(`Inativar "${this.selectedProcess.title}"?`)) {
      this.service.update(this.selectedProcess.id, { ...this.selectedProcess, active: false }).subscribe(() => this.loadProcesses());
    }
  }

  generatePDF() {
    const doc = new jsPDF('p', 'mm', 'a4');
    doc.text('Relatório de Processos', 14, 15);
    autoTable(doc, { 
      startY: 20, 
      head: [['Título', 'Período', 'Início', 'Fim', 'Status']], 
      body: this.filteredProcesses.map(p => [p.title, this.getPeriodInfo(p.academicPeriodId), this.datePipe.transform(p.startDate, 'dd/MM/yyyy'), this.datePipe.transform(p.endDate, 'dd/MM/yyyy'), p.active ? 'Ativo' : 'Inativo']) 
    });
    doc.save('processos.pdf');
  }

  generateLinkedClassesPDF() {
    if (!this.selectedProcess || this.linkedClasses.length === 0) return;

    const doc = new jsPDF();
    doc.setFontSize(16);
    doc.text(`Turmas Vinculadas: ${this.selectedProcess.title}`, 14, 15);
    
    const tableData = this.linkedClasses.map(link => [
      this.getClassGroupName(link.classGroupId)
    ]);

    autoTable(doc, { 
      startY: 20, 
      head: [['Nome da Turma']], 
      body: tableData,
      theme: 'striped',
      headStyles: { fillColor: [35, 32, 102] }
    });

    doc.save(`turmas-${this.selectedProcess.title.replace(/\s+/g, '-')}.pdf`);
  }
}