package br.edu.uesb.prematricula.enrollmentprocess.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.uesb.prematricula.enrollmentprocess.model.dto.request.CreateEnrollmentProcessRequestDTO;
import br.edu.uesb.prematricula.enrollmentprocess.model.dto.request.UpdateEnrollmentProcessRequestDTO;
import br.edu.uesb.prematricula.enrollmentprocess.model.dto.response.EnrollmentProcessResponseDTO;
import br.edu.uesb.prematricula.enrollmentprocess.service.EnrollmentProcessService;
import br.edu.uesb.prematricula.enrollmentprocessclasses.model.dto.response.EnrollmentProcessClassResponseDTO;
import br.edu.uesb.prematricula.enrollmentprocessclasses.service.EnrollmentProcessClassService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador responsável pelo gerenciamento de processos de matrícula.
 *
 * <p>
 * Disponibiliza endpoints REST para criação, consulta,
 * atualização e desativação de períodos/processos de matrícula,
 * bem como acesso às turmas ativas do processo.
 * </p>
 * \n *
 * <p>
 * Implementa controle de acesso granular: administradores possuem
 * acesso completo de escrita; estudantes e administradores podem
 * consultar processos abertos.
 * </p>
 */
@RestController
@RequestMapping("/enrollment-processes")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class EnrollmentProcessController {

    private final EnrollmentProcessService enrollmentProcessService;
    private final EnrollmentProcessClassService enrollmentProcessClassService;

    /**
     * Cadastra um novo processo de matrícula.
     *
     * <p>
     * Apenas administradores podem executar esta operação.
     * Apenas um processo pode estar aberto por vez.
     * </p>
     *
     * @param dto dados do processo de matrícula
     * @return processo criado com status HTTP 201
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentProcessResponseDTO> create(
            @Valid @RequestBody CreateEnrollmentProcessRequestDTO dto) {

        EnrollmentProcessResponseDTO response = enrollmentProcessService.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retorna todos os processos de matrícula cadastrados.
     *
     * <p>
     * Apenas administradores podem executar esta operação.
     * </p>
     *
     * @return lista de processos de matrícula
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentProcessResponseDTO>> findAll() {

        return ResponseEntity.ok(enrollmentProcessService.findAll());

    }

    /**
     * Busca um processo de matrícula pelo identificador.
     *
     * <p>
     * Apenas administradores podem executar esta operação.
     * </p>
     *
     * @param id identificador do processo de matrícula
     * @return processo encontrado
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentProcessResponseDTO> findById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(enrollmentProcessService.findById(id));

    }

    /**
     * Atualiza os dados de um processo de matrícula existente.
     *
     * <p>
     * Apenas administradores podem executar esta operação.
     * Apenas processos com status NOT_STARTED podem ser atualizados.
     * </p>
     *
     * @param id  identificador do processo de matrícula
     * @param dto novos dados do processo
     * @return processo atualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentProcessResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEnrollmentProcessRequestDTO dto) {

        return ResponseEntity.ok(
                enrollmentProcessService.update(id, dto));
    }

    /**
     * Desativa um processo de matrícula.
     *
     * <p>
     * Apenas administradores podem executar esta operação.
     * A operação realiza exclusão lógica, preservando o histórico.
     * </p>
     *
     * @param id identificador do processo a ser desativado
     * @return sem conteúdo (HTTP 204)
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(
            @PathVariable UUID id) {

        enrollmentProcessService.deactivate(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * Retorna o único processo de matrícula aberto (em OPEN status).
     *
     * <p>
     * Disponível para estudantes e administradores autenticados.
     * Apenas um processo pode estar aberto por vez.
     * </p>
     *
     * @return processo aberto
     */
    @GetMapping("/open")
    @PreAuthorize("hasRole('STUDENT', 'ADMIN')")
    public ResponseEntity<EnrollmentProcessResponseDTO> findOpenProcess() {
        return ResponseEntity.ok(
                enrollmentProcessService.findOpenProcess());
    }

    /**
     * Retorna todas as turmas do processo de matrícula aberto.
     *
     * <p>
     * Disponível para estudantes e administradores autenticados.
     * Retorna apenas turmas associadas ao processo que está em OPEN status.
     * </p>
     *
     * @return lista de turmas do processo aberto
     */
    @GetMapping("/open/classes")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<List<EnrollmentProcessClassResponseDTO>> findOpenClasses() {

        return ResponseEntity.ok(
                enrollmentProcessClassService.findOpenProcessClasses());
    }

}
