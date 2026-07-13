package br.edu.uesb.prematricula.enrollment.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.uesb.prematricula.enrollment.model.dto.request.CreateEnrollmentRequestDTO;
import br.edu.uesb.prematricula.enrollment.model.dto.request.UpdateEnrollmentRequestDTO;
import br.edu.uesb.prematricula.enrollment.model.dto.response.EnrollmentResponseDTO;
import br.edu.uesb.prematricula.enrollment.service.EnrollmentService;
import br.edu.uesb.prematricula.user.model.entity.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<EnrollmentResponseDTO> create(
            Authentication authentication,
            @Valid @RequestBody CreateEnrollmentRequestDTO dto) {

        User authenticatedUser = (User) authentication.getPrincipal();

        EnrollmentResponseDTO response = enrollmentService.create(
                authenticatedUser,
                dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<EnrollmentResponseDTO> update(
            Authentication authentication,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEnrollmentRequestDTO dto) {

        User authenticatedUser = (User) authentication.getPrincipal();

        EnrollmentResponseDTO response = enrollmentService.update(
                authenticatedUser,
                id,
                dto);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> cancel(
            Authentication authentication,
            @PathVariable UUID id) {

        User authenticatedUser = (User) authentication.getPrincipal();

        enrollmentService.cancel(
                authenticatedUser,
                id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<EnrollmentResponseDTO> findMyEnrollment(
            Authentication authentication) {

        User authenticatedUser = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                enrollmentService.findMyEnrollment(
                        authenticatedUser));
    }

    @GetMapping("/me/history")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<EnrollmentResponseDTO>> findMyHistory(
            Authentication authentication) {

        User authenticatedUser = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                enrollmentService.findMyHistory(
                        authenticatedUser));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentResponseDTO> findById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                enrollmentService.findById(id));
    }

    @GetMapping("/process/{processId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponseDTO>> findByProcess(
            @PathVariable UUID processId) {

        return ResponseEntity.ok(
                enrollmentService.findByProcess(
                        processId));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponseDTO>> findByStudent(
            @PathVariable UUID studentId) {

        return ResponseEntity.ok(
                enrollmentService.findByStudent(
                        studentId));
    }

}