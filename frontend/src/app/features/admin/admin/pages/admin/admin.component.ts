import {
  ChangeDetectorRef,
  Component,
  OnInit,
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { AdminService } from '../../services/admin.service';
import { Admin } from '../../models/admin.model';

import { AdminModalComponent } from '../../components/modal/admin-modal.component';

import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

@Component({
  selector: 'app-admin-management',
  standalone: true,

  imports: [
    CommonModule,
    FormsModule,
    AdminModalComponent,
  ],

  templateUrl: './admin.html',
})
export class AdminComponent
  implements OnInit {

  admins: Admin[] = [];
  filteredAdmins: Admin[] = [];

  searchTerm = '';

  selectedAdmin: Admin | null = null;

  isModalOpen = false;
  isLoading = false;

  errorMessage: string | null = null;

  constructor(
    private readonly adminService:
      AdminService,

    private readonly cdr:
      ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadAdmins();
  }

  loadAdmins(): void {
    this.isLoading = true;
    this.errorMessage = null;

    this.adminService
      .getAll()
      .subscribe({
        next: data => {
          this.admins =
            [...data].sort(
              (first, second) => {
                if (
                  first.active ===
                  second.active
                ) {
                  return first.fullName.localeCompare(
                    second.fullName,
                    'pt-BR',
                    {
                      sensitivity: 'base',
                    },
                  );
                }

                return first.active
                  ? -1
                  : 1;
              },
            );

          this.selectedAdmin = null;

          this.onSearch();

          this.isLoading = false;

          this.cdr.detectChanges();
        },

        error: error => {
          console.error(
            'Erro ao carregar administradores:',
            error,
          );

          this.errorMessage =
            'Não foi possível carregar os administradores.';

          this.isLoading = false;

          this.cdr.detectChanges();
        },
      });
  }

  onSearch(): void {
    const term =
      this.searchTerm
        .trim()
        .toLocaleLowerCase('pt-BR');

    this.filteredAdmins =
      this.admins.filter(admin =>
        admin.fullName
          .toLocaleLowerCase('pt-BR')
          .includes(term) ||
        admin.email
          .toLocaleLowerCase('pt-BR')
          .includes(term),
      );
  }

  selectAdmin(
    admin: Admin,
  ): void {
    this.selectedAdmin =
      this.selectedAdmin?.id === admin.id
        ? null
        : admin;
  }

  openCreateModal(): void {
    this.selectedAdmin = null;
    this.isModalOpen = true;
  }

  openUpdateModal(): void {
    if (this.selectedAdmin) {
      this.isModalOpen = true;
    }
  }

  inactivateAdmin(): void {
    const selectedAdmin =
      this.selectedAdmin;

    const adminId =
      selectedAdmin?.id;

    if (
      !selectedAdmin ||
      !adminId
    ) {
      return;
    }

    if (!selectedAdmin.active) {
      return;
    }

    const confirmed =
      confirm(
        `Deseja inativar o administrador ${selectedAdmin.fullName}?`,
      );

    if (!confirmed) {
      return;
    }

    this.adminService
      .delete(adminId)
      .subscribe({
        next: () => {
          this.loadAdmins();
        },

        error: error => {
          console.error(
            'Erro ao inativar administrador:',
            error,
          );

          this.errorMessage =
            error?.error?.message ??
            'Não foi possível inativar o administrador.';

          this.cdr.detectChanges();
        },
      });
  }

  generatePDF(): void {
    const document =
      new jsPDF();

    document.setFontSize(16);

    document.text(
      'Relatório de Administradores',
      14,
      15,
    );

    const tableData =
      this.filteredAdmins.map(admin => [
        admin.fullName,
        admin.email,
        admin.active
          ? 'Ativo'
          : 'Inativo',
      ]);

    autoTable(document, {
      startY: 20,

      head: [[
        'Nome',
        'E-mail',
        'Status',
      ]],

      body: tableData,

      theme: 'grid',

      headStyles: {
        fillColor: [
          35,
          32,
          102,
        ],
      },
    });

    document.save(
      'lista-administradores.pdf',
    );
  }

  onModalSuccess(): void {
    this.isModalOpen = false;
    this.loadAdmins();
  }
}