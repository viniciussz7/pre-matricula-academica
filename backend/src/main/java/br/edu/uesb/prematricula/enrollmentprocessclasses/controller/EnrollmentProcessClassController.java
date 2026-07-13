package br.edu.uesb.prematricula.enrollmentprocessclasses.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import br.edu.uesb.prematricula.enrollmentprocessclasses.model.dto.request.CreateEnrollmentProcessClassRequestDTO;
import br.edu.uesb.prematricula.enrollmentprocessclasses.model.dto.response.EnrollmentProcessClassResponseDTO;
import br.edu.uesb.prematricula.enrollmentprocessclasses.service.EnrollmentProcessClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/enrollment-process-classes")
@RequiredArgsConstructor
public class EnrollmentProcessClassController {

    private final EnrollmentProcessClassService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentProcessClassResponseDTO> create(
            @Valid @RequestBody CreateEnrollmentProcessClassRequestDTO dto) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(dto));
    }

    @GetMapping("/process/{processId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentProcessClassResponseDTO>>
    findByProcess(
            @PathVariable UUID processId) {

        return ResponseEntity.ok(
                service.findByProcess(processId));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(
            @PathVariable UUID id) {

        service.deactivate(id);

        return ResponseEntity.noContent().build();
    }

}