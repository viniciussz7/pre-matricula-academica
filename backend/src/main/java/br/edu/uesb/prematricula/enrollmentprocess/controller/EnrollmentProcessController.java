package br.edu.uesb.prematricula.enrollmentprocess.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.uesb.prematricula.enrollmentprocess.model.dto.request.CreateEnrollmentProcessRequestDTO;
import br.edu.uesb.prematricula.enrollmentprocess.model.dto.request.UpdateEnrollmentProcessRequestDTO;
import br.edu.uesb.prematricula.enrollmentprocess.model.dto.response.EnrollmentProcessResponseDTO;
import br.edu.uesb.prematricula.enrollmentprocess.service.EnrollmentProcessService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/enrollment-processes")
@RequiredArgsConstructor
public class EnrollmentProcessController {

    private final EnrollmentProcessService enrollmentProcessService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentProcessResponseDTO> create(
            @Valid @RequestBody CreateEnrollmentProcessRequestDTO dto) {

        EnrollmentProcessResponseDTO response = enrollmentProcessService.create(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentProcessResponseDTO>> findAll() {

        return ResponseEntity.ok(enrollmentProcessService.findAll());

    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentProcessResponseDTO> findById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(enrollmentProcessService.findById(id));

    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentProcessResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEnrollmentProcessRequestDTO dto) {

        return ResponseEntity.ok(
                enrollmentProcessService.update(id, dto));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(
            @PathVariable UUID id) {

        enrollmentProcessService.deactivate(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/open")
    @PreAuthorize("hasRole('STUDENT', 'ADMIN')")
    public ResponseEntity<EnrollmentProcessResponseDTO> findOpenProcess() {
        return ResponseEntity.ok(
                enrollmentProcessService.findOpenProcess());
    }

}
