package br.edu.uesb.prematricula.reports.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.uesb.prematricula.reports.model.dto.response.ClassDemandResponseDTO;
import br.edu.uesb.prematricula.reports.model.dto.response.EnrolledStudentResponseDTO;
import br.edu.uesb.prematricula.reports.model.dto.response.ProcessSummaryResponseDTO;
import br.edu.uesb.prematricula.reports.model.dto.response.StudentWithoutEnrollmentResponseDTO;
import br.edu.uesb.prematricula.reports.service.ReportService;
import lombok.RequiredArgsConstructor;

/**
 * Controlador responsável pela geração de relatórios de matrículas.
 *
 * <p>
 * Disponibiliza endpoints REST para consulta de dados estatísticos
 * e analíticos dos processos de matrícula do sistema de
 * Pré-Matrícula Acadêmica.
 * </p>
 *
 * <p>
 * Inclui relatórios de demanda por turma, estudantes inscritos,
 * resumos estatísticos e identificação de estudantes sem matrícula.
 * </p>
 *
 * <p>
 * Os endpoints são protegidos pelo Spring Security,
 * permitindo acesso apenas a administradores autenticados.
 * </p>
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

        private final ReportService reportService;

        /**
         * Retorna relatório de demanda por turma em um processo de matrícula.
         *
         * <p>
         * Apenas administradores podem executar esta operação.
         * Apresenta informações de ocupação, vagas disponíveis e
         * percentual de ocupação para cada turma ativa no processo.
         * </p>
         *
         * @param processId identificador do processo de matrícula
         * @return lista com informações de demanda por turma
         */
        @GetMapping("/processes/{processId}/class-demand")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<List<ClassDemandResponseDTO>> findClassDemand(
                        @PathVariable UUID processId) {

                return ResponseEntity.ok(
                                reportService.findClassDemand(processId));
        }

        /**
         * Retorna relatório de estudantes inscritos em uma turma do processo.
         *
         * <p>
         * Apenas administradores podem executar esta operação.
         * Retorna lista de todos os estudantes com matrículas ativas
         * inscritos na turma, ordenados alfabeticamente por nome.
         * </p>
         *
         * @param processClassId identificador da turma do processo
         * @return lista de estudantes inscritos na turma
         */
        @GetMapping("/process-classes/{processClassId}/students")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<List<EnrolledStudentResponseDTO>> findStudentsByProcessClass(
                        @PathVariable UUID processClassId) {

                return ResponseEntity.ok(
                                reportService.findStudentsByProcessClass(
                                                processClassId));
        }

        /**
         * Retorna relatório resumido estatístico de um processo de matrícula.
         *
         * <p>
         * Apenas administradores podem executar esta operação.
         * Apresenta informações agregadas: total de matrículas (ativas e canceladas),
         * total de seleções, média de turmas por matrícula, número de turmas cheias
         * e turmas com sobrecarga, além da turma mais procurada.
         * </p>
         *
         * @param processId identificador do processo de matrícula
         * @return resumo estatístico do processo
         */
        @GetMapping("/processes/{processId}/summary")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ProcessSummaryResponseDTO> findProcessSummary(
                        @PathVariable UUID processId) {

                return ResponseEntity.ok(
                                reportService.findProcessSummary(processId));
        }

        /**
         * Retorna relatório de estudantes sem matrícula ativa em um processo.
         *
         * <p>
         * Apenas administradores podem executar esta operação.
         * Retorna lista de todos os estudantes ativos que não possuem
         * matrícula ativa no processo de matrícula, ordenados por nome.
         * </p>
         *
         * @param processId identificador do processo de matrícula
         * @return lista de estudantes sem matrícula no processo
         */
        @GetMapping("/processes/{processId}/students-without-enrollment")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<List<StudentWithoutEnrollmentResponseDTO>> findStudentsWithoutEnrollment(
                        @PathVariable UUID processId) {

                return ResponseEntity.ok(
                                reportService.findStudentsWithoutEnrollment(
                                                processId));
        }

}
