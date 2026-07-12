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

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentResponseDTO> create(
            @Valid @RequestBody CreateStudentRequestDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(studentService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<StudentResponseDTO>> findAll() {

        return ResponseEntity.ok(studentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDTO> findById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(studentService.findById(id));
    }

}