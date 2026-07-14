package br.edu.uesb.prematricula.student.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.edu.uesb.prematricula.student.model.dto.request.CreateStudentRequestDTO;
import br.edu.uesb.prematricula.student.model.dto.response.StudentResponseDTO;
import br.edu.uesb.prematricula.student.service.StudentService;
import jakarta.validation.Valid;
import lombok.*;

/**
 * Controlador responsável pelo gerenciamento de estudantes.
 *
 * <p>
 * Disponibiliza endpoints REST para cadastro e consulta
 * de estudantes do sistema de Pré-Matrícula Acadêmica,
 * mantendo referência com usuários e números de matrícula.
 * </p>
 *
 * <p>
 * Os endpoints são protegidos pelo Spring Security,
 * conforme as regras definidas na configuração da aplicação.
 * </p>
 */
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class StudentController {

    private final StudentService studentService;

    /**
     * Cadastra um novo estudante.
     *
     * <p>
     * Apenas administradores podem executar esta operação.
     * O número de matrícula deve ser único no sistema.
     * </p>
     *
     * @param dto dados do estudante
     * @return estudante criado com status HTTP 201
     */
    @PostMapping
    public ResponseEntity<StudentResponseDTO> create(
            @Valid @RequestBody CreateStudentRequestDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(studentService.create(dto));
    }

    /**
     * Retorna todos os estudantes cadastrados.
     *
     * <p>
     * Disponível para administradores autenticados.
     * </p>
     *
     * @return lista de estudantes
     */
    @GetMapping
    public ResponseEntity<List<StudentResponseDTO>> findAll() {

        return ResponseEntity.ok(studentService.findAll());
    }

    /**
     * Busca um estudante pelo identificador.
     *
     * <p>
     * Disponível para administradores autenticados.
     * </p>
     *
     * @param id identificador do estudante
     * @return estudante encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> findById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(studentService.findById(id));
    }

}