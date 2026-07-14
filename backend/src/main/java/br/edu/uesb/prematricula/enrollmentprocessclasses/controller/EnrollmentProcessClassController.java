package br.edu.uesb.prematricula.enrollmentprocessclasses.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import br.edu.uesb.prematricula.enrollmentprocessclasses.model.dto.request.CreateEnrollmentProcessClassRequestDTO;
import br.edu.uesb.prematricula.enrollmentprocessclasses.model.dto.response.EnrollmentProcessClassResponseDTO;
import br.edu.uesb.prematricula.enrollmentprocessclasses.service.EnrollmentProcessClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador responsável pela associação de turmas a processos de matrícula.
 *
 * <p>
 * Disponibiliza endpoints REST para criar associações entre turmas
 * (class groups) e processos de matrícula, consultar turmas de um processo
 * e desativar associações.
 * </p>
 *
 * <p>
 * Os endpoints são protegidos pelo Spring Security,
 * permitindo acesso apenas a administradores para operações de escrita.
 * </p>
 */
@RestController
@RequestMapping("/enrollment-process-classes")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class EnrollmentProcessClassController {

    private final EnrollmentProcessClassService service;

    /**
     * Associa uma turma a um processo de matrícula.
     *
     * <p>
     * Apenas administradores podem executar esta operação.
     * Permite reativar uma associação previamente desativada.
     * </p>
     *
     * @param dto dados para criar a associação (turma e processo)
     * @return associação criada com status HTTP 201
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentProcessClassResponseDTO> create(
            @Valid @RequestBody CreateEnrollmentProcessClassRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto));
    }

    /**
     * Retorna todas as turmas associadas a um processo de matrícula.
     *
     * <p>
     * Apenas administradores podem executar esta operação.
     * Retorna todas as associações ativas do processo.
     * </p>
     *
     * @param processId identificador do processo de matrícula
     * @return lista de turmas do processo
     */
    @GetMapping("/process/{processId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentProcessClassResponseDTO>> findByProcess(
            @PathVariable UUID processId) {

        return ResponseEntity.ok(
                service.findByProcess(processId));
    }

    /**
     * Desativa a associação entre turma e processo de matrícula.
     *
     * <p>
     * Apenas administradores podem executar esta operação.
     * A operação realiza exclusão lógica, preservando o histórico.
     * </p>
     *
     * @param id identificador da associação a ser desativada
     * @return sem conteúdo (HTTP 204)
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(
            @PathVariable UUID id) {

        service.deactivate(id);

        return ResponseEntity.noContent().build();
    }

}