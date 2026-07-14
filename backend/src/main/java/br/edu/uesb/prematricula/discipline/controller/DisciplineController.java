package br.edu.uesb.prematricula.discipline.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.edu.uesb.prematricula.discipline.model.dto.request.DisciplineRequestDTO;
import br.edu.uesb.prematricula.discipline.model.dto.response.DisciplineResponseDTO;
import br.edu.uesb.prematricula.discipline.service.DisciplineService;

import java.util.List;
import java.util.UUID;

/**
 * Controlador responsável pelo gerenciamento de disciplinas.
 *
 * <p>
 * Disponibiliza endpoints REST para criação, consulta,
 * atualização e inativação de disciplinas acadêmicas do sistema
 * de Pré-Matrícula Acadêmica.
 * </p>
 *
 * <p>
 * Os endpoints são protegidos pelo Spring Security,
 * conforme as regras definidas na configuração da aplicação.
 * </p>
 */
@RestController
@RequestMapping("/disciplines")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class DisciplineController {

    private final DisciplineService service;

    /**
     * Cadastra uma nova disciplina.
     *
     * <p>
     * Apenas administradores podem executar esta operação.
     * </p>
     *
     * @param dto dados da disciplina
     * @return disciplina criada com status HTTP 201
     */
    @PostMapping
    public ResponseEntity<DisciplineResponseDTO> create(@RequestBody @Valid DisciplineRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    /**
     * Atualiza os dados de uma disciplina existente.
     *
     * <p>
     * Apenas administradores podem executar esta operação.
     * </p>
     *
     * @param id  identificador da disciplina
     * @param dto novos dados da disciplina
     * @return disciplina atualizada
     */
    @PutMapping("/{id}")
    public ResponseEntity<DisciplineResponseDTO> update(@PathVariable UUID id,
            @RequestBody @Valid DisciplineRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    /**
     * Retorna todas as disciplinas cadastradas.
     *
     * <p>
     * Disponível para administradores autenticados.
     * </p>
     *
     * @return lista de disciplinas
     */
    @GetMapping
    public ResponseEntity<List<DisciplineResponseDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Busca uma disciplina pelo identificador.
     *
     * <p>
     * Disponível para administradores autenticados.
     * </p>
     *
     * @param id identificador da disciplina
     * @return disciplina encontrada
     */
    @GetMapping("/{id}")
    public ResponseEntity<DisciplineResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Desativa uma disciplina.
     *
     * <p>
     * Apenas administradores podem executar esta operação.
     * A operação realiza exclusão lógica, preservando o histórico.
     * </p>
     *
     * @param id identificador da disciplina a ser desativada
     * @return sem conteúdo (HTTP 204)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}