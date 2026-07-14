package br.edu.uesb.prematricula.enrollment.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.uesb.prematricula.enrollment.model.dto.request.CreateEnrollmentRequestDTO;
import br.edu.uesb.prematricula.enrollment.model.dto.request.UpdateEnrollmentRequestDTO;
import br.edu.uesb.prematricula.enrollment.model.dto.response.EnrollmentResponseDTO;
import br.edu.uesb.prematricula.enrollment.service.EnrollmentService;
import br.edu.uesb.prematricula.user.model.entity.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador responsável pelo gerenciamento de matrículas de estudantes.
 *
 * <p>
 * Disponibiliza endpoints REST para criação, atualização, cancelamento
 * e consulta de matrículas do sistema de Pré-Matrícula Acadêmica.
 * Implementa controle de acesso baseado em papéis (STUDENT, ADMIN).
 * </p>
 *
 * <p>
 * Estudantes podem gerenciar suas próprias matrículas, enquanto
 * administradores possuem acesso completo para consulta e análise.
 * </p>
 */
@RestController
@RequestMapping("/enrollments")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class EnrollmentController {

        private final EnrollmentService enrollmentService;

        /**
         * Cadastra uma nova matrícula para o estudante autenticado.
         *
         * <p>
         * Apenas estudantes autenticados podem executar esta operação.
         * Valida limite máximo de turmas (7), impede disciplinas duplicadas
         * e respeita o limite de vagas considerando a configuração de
         * sobrecarga da turma.
         * </p>
         *
         * @param authentication contexto de autenticação do estudante
         * @param dto            dados da matrícula incluindo seleções de turmas
         * @return matrícula criada com status HTTP 201
         */
        @PostMapping
        @PreAuthorize("hasRole('STUDENT')")
        public ResponseEntity<EnrollmentResponseDTO> create(
                        Authentication authentication,
                        @Valid @RequestBody CreateEnrollmentRequestDTO dto) {

                User authenticatedUser = (User) authentication.getPrincipal();

                EnrollmentResponseDTO response = enrollmentService.create(
                                authenticatedUser,
                                dto);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response);
        }

        /**
         * Atualiza as seleções de turmas de uma matrícula existente.
         *
         * <p>
         * Apenas o estudante proprietário da matrícula pode executar
         * esta operação. Reaplica todas as validações de negócio
         * (limite de 7 turmas, sem disciplinas duplicadas, capacidade).
         * </p>
         *
         * @param authentication contexto de autenticação do estudante
         * @param id             identificador da matrícula
         * @param dto            novas seleções de turmas
         * @return matrícula atualizada
         */
        @PutMapping("/{id}")
        @PreAuthorize("hasRole('STUDENT')")
        public ResponseEntity<EnrollmentResponseDTO> update(
                        Authentication authentication,
                        @PathVariable UUID id,
                        @Valid @RequestBody UpdateEnrollmentRequestDTO dto) {

                User authenticatedUser = (User) authentication.getPrincipal();

                EnrollmentResponseDTO response = enrollmentService.update(
                                authenticatedUser,
                                id,
                                dto);

                return ResponseEntity.ok(response);
        }

        /**
         * Cancela uma matrícula do estudante autenticado.
         *
         * <p>
         * Apenas o estudante proprietário da matrícula pode executar
         * esta operação. A operação realiza exclusão lógica,
         * preservando o histórico de matrículas.
         * </p>
         *
         * @param authentication contexto de autenticação do estudante
         * @param id             identificador da matrícula a ser cancelada
         * @return sem conteúdo (HTTP 204)
         */
        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('STUDENT')")
        public ResponseEntity<Void> cancel(
                        Authentication authentication,
                        @PathVariable UUID id) {

                User authenticatedUser = (User) authentication.getPrincipal();

                enrollmentService.cancel(
                                authenticatedUser,
                                id);

                return ResponseEntity.noContent().build();
        }

        /**
         * Retorna a matrícula ativa do estudante autenticado.
         *
         * <p>
         * Apenas estudantes autenticados podem executar esta operação.
         * Cada estudante possui uma única matrícula ativa por período.
         * </p>
         *
         * @param authentication contexto de autenticação do estudante
         * @return matrícula ativa do estudante
         */
        @GetMapping("/me")
        @PreAuthorize("hasRole('STUDENT')")
        public ResponseEntity<EnrollmentResponseDTO> findMyEnrollment(
                        Authentication authentication) {

                User authenticatedUser = (User) authentication.getPrincipal();

                return ResponseEntity.ok(
                                enrollmentService.findMyEnrollment(
                                                authenticatedUser));
        }

        /**
         * Retorna o histórico de matrículas do estudante autenticado.
         *
         * <p>
         * Apenas estudantes autenticados podem executar esta operação.
         * Retorna todas as matrículas (ativas e canceladas) do estudante.
         * </p>
         *
         * @param authentication contexto de autenticação do estudante
         * @return lista com histórico de matrículas
         */
        @GetMapping("/me/history")
        @PreAuthorize("hasRole('STUDENT')")
        public ResponseEntity<List<EnrollmentResponseDTO>> findMyHistory(
                        Authentication authentication) {

                User authenticatedUser = (User) authentication.getPrincipal();

                return ResponseEntity.ok(
                                enrollmentService.findMyHistory(
                                                authenticatedUser));
        }

        /**
         * Busca uma matrícula pelo identificador.
         *
         * <p>
         * Apenas administradores podem executar esta operação.
         * </p>
         *
         * @param id identificador da matrícula
         * @return matrícula encontrada
         */
        @GetMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<EnrollmentResponseDTO> findById(
                        @PathVariable UUID id) {

                return ResponseEntity.ok(
                                enrollmentService.findById(id));
        }

        /**
         * Retorna todas as matrículas de um processo de matrícula.
         *
         * <p>
         * Apenas administradores podem executar esta operação.
         * Útil para relatórios e análise de dados do processo.
         * </p>
         *
         * @param processId identificador do processo de matrícula
         * @return lista de matrículas do processo
         */
        @GetMapping("/process/{processId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<List<EnrollmentResponseDTO>> findByProcess(
                        @PathVariable UUID processId) {

                return ResponseEntity.ok(
                                enrollmentService.findByProcess(
                                                processId));
        }

        /**
         * Retorna todas as matrículas de um estudante.
         *
         * <p>
         * Apenas administradores podem executar esta operação.
         * Retorna matrículas ativas e canceladas do estudante.
         * </p>
         *
         * @param studentId identificador do estudante
         * @return lista de matrículas do estudante
         */
        @GetMapping("/student/{studentId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<List<EnrollmentResponseDTO>> findByStudent(
                        @PathVariable UUID studentId) {

                return ResponseEntity.ok(
                                enrollmentService.findByStudent(
                                                studentId));
        }

}