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

@RestController
@RequestMapping("/class-groups")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ClassGroupController {

    private ClassGroupService service;

    @PostMapping
    public ResponseEntity<ClassGroupResponseDTO> create(@RequestBody @Valid ClassGroupRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClassGroupResponseDTO> update(@PathVariable UUID id, @RequestBody @Valid ClassGroupRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping
    public ResponseEntity<List<ClassGroupResponseDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassGroupResponseDTO> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}