package br.edu.uesb.prematricula.academicperiod.controller;

import br.edu.uesb.prematricula.academicperiod.model.dto.request.AcademicPeriodRequestDTO;
import br.edu.uesb.prematricula.academicperiod.model.dto.response.AcademicPeriodResponseDTO;
import br.edu.uesb.prematricula.academicperiod.service.AcademicPeriodService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/academic-periods")
@CrossOrigin(origins = "http://localhost:4200")
public class AcademicPeriodController {

   @Autowired
    private AcademicPeriodService service;

    @PostMapping
    public ResponseEntity<AcademicPeriodResponseDTO> create(@RequestBody @Valid AcademicPeriodRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AcademicPeriodResponseDTO> update(@PathVariable UUID id, @RequestBody @Valid AcademicPeriodRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping
    public ResponseEntity<List<AcademicPeriodResponseDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AcademicPeriodResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}