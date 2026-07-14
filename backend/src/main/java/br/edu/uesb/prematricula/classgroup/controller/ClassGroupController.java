package br.edu.uesb.prematricula.classgroup.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.edu.uesb.prematricula.classgroup.model.dto.request.ClassGroupRequestDTO;
import br.edu.uesb.prematricula.classgroup.model.dto.response.ClassGroupResponseDTO;
import br.edu.uesb.prematricula.classgroup.service.ClassGroupService;

import java.util.List;
import java.util.UUID;

/**
 * Controlador responsável pelo gerenciamento de turmas.
 *
 * <p>
 * Disponibiliza endpoints REST para criação, consulta,
 * atualização e inativação de turmas (class groups) do sistema
 * de Pré-Matrícula Acadêmica.
 * </p>
 *
 * <p>
 * Os endpoints são protegidos pelo Spring Security,
 * conforme as regras definidas na configuração da aplicação.
 * </p>
 */
@RestController
@RequestMapping("/class-groups")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ClassGroupController {

    private final ClassGroupService service;

    /**
     * Cadastra uma nova turma.
     *
     * <p>
     * Apenas administradores podem executar esta operação.
     * A combinação de código e período acadêmico deve ser única.
     * </p>
     *
     * @param dto dados da turma
     * @return turma criada com status HTTP 201
     */
    @PostMapping
    public ResponseEntity<ClassGroupResponseDTO> create(@RequestBody @Valid ClassGroupRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    /**
     * Atualiza os dados de uma turma existente.
     *
     * <p>
     * Apenas administradores podem executar esta operação.
     * </p>
     *
     * @param id  identificador da turma
     * @param dto novos dados da turma
     * @return turma atualizada
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClassGroupResponseDTO> update(@PathVariable UUID id,
            @RequestBody @Valid ClassGroupRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    /**
     * Retorna todas as turmas cadastradas.
     *
     * <p>
     * Disponível para administradores autenticados.
     * </p>
     *
     * @return lista de turmas
     */
    @GetMapping
    public ResponseEntity<List<ClassGroupResponseDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Busca uma turma pelo identificador.
     *
     * <p>
     * Disponível para administradores autenticados.
     * </p>
     *
     * @param id identificador da turma
     * @return turma encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClassGroupResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Desativa uma turma.
     *
     * <p>
     * Apenas administradores podem executar esta operação.
     * A operação realiza exclusão lógica, preservando o histórico.
     * </p>
     *
     * @param id identificador da turma a ser desativada
     * @return sem conteúdo (HTTP 204)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}