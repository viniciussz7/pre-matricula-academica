package br.edu.uesb.prematricula.discipline.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.edu.uesb.prematricula.discipline.model.dto.request.DisciplineRequestDTO;
import br.edu.uesb.prematricula.discipline.model.dto.response.DisciplineResponseDTO;
import br.edu.uesb.prematricula.discipline.service.DisciplineService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/disciplines")
public class DisciplineController {

    @Autowired
    private DisciplineService service;

    @PostMapping
    public ResponseEntity<DisciplineResponseDTO> create(@RequestBody @Valid DisciplineRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DisciplineResponseDTO> update(@PathVariable UUID id, @RequestBody @Valid DisciplineRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping
    public ResponseEntity<List<DisciplineResponseDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisciplineResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}