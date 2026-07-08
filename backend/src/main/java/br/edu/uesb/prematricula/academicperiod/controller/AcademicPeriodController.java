package br.edu.uesb.prematricula.academicperiod.controller;

import br.edu.uesb.prematricula.academicperiod.model.dto.request.AcademicPeriodRequestDTO;
import br.edu.uesb.prematricula.academicperiod.model.entity.AcademicPeriod;
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
public class AcademicPeriodController {

   @Autowired
    private AcademicPeriodService service;

    @PostMapping
    public ResponseEntity<AcademicPeriod> create(@RequestBody @Valid AcademicPeriodRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AcademicPeriod> update(@PathVariable UUID id, @RequestBody @Valid AcademicPeriodRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping
    public ResponseEntity<List<AcademicPeriod>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AcademicPeriod> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}