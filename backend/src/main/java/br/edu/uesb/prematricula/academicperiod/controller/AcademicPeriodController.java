package br.edu.uesb.prematricula.academicperiod.controller;

import br.edu.uesb.prematricula.academicperiod.model.dto.request.AcademicPeriodRequestDTO;
import br.edu.uesb.prematricula.academicperiod.model.dto.response.AcademicPeriodResponseDTO;
import br.edu.uesb.prematricula.academicperiod.service.AcademicPeriodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador responsável pelo gerenciamento dos períodos letivos.
 *
 * <p>
 * Disponibiliza endpoints REST para criação, consulta,
 * atualização e inativação de períodos letivos do sistema
 * de Pré-Matrícula Acadêmica.
 * </p>
 *
 * <p>
 * Os endpoints são protegidos pelo Spring Security,
 * conforme as regras definidas na configuração da aplicação.
 * </p>
 */
@RestController
@RequestMapping("/academic-periods")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AcademicPeriodController {

    private final AcademicPeriodService service;

    /**
     * Cadastra um novo período letivo.
     *
     * <p>
     * Apenas administradores podem executar esta operação.
     * </p>
     *
     * @param dto dados do período letivo
     * @return período letivo criado com status HTTP 201
     */
    @PostMapping
    public ResponseEntity<AcademicPeriodResponseDTO> create(@RequestBody @Valid AcademicPeriodRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    /**
     * Atualiza os dados de um período letivo existente.
     *
     * <p>
     * Apenas administradores podem executar esta operação.
     * </p>
     *
     * @param id  identificador do período letivo
     * @param dto novos dados do período letivo
     * @return período letivo atualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<AcademicPeriodResponseDTO> update(@PathVariable UUID id,
            @RequestBody @Valid AcademicPeriodRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    /**
     * Retorna todos os períodos letivos cadastrados.
     *
     * <p>
     * Disponível para administradores autenticados.
     * </p>
     *
     * @return lista de períodos letivos
     */
    @GetMapping
    public ResponseEntity<List<AcademicPeriodResponseDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    /**
     * Busca um período letivo pelo identificador.
     *
     * <p>
     * Disponível para administradores autenticados.
     * </p>
     *
     * @param id identificador do período letivo
     * @return período letivo encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<AcademicPeriodResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Desativa um período letivo.
     *
     * <p>
     * Apenas administradores podem executar esta operação.
     * A operação realiza exclusão lógica, preservando o histórico.
     * </p>
     *
     * @param id identificador do período letivo a ser desativado
     * @return sem conteúdo (HTTP 204)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}