package br.edu.uesb.prematricula.reports.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.uesb.prematricula.reports.model.dto.response.ClassDemandResponseDTO;
import br.edu.uesb.prematricula.reports.model.dto.response.EnrolledStudentResponseDTO;
import br.edu.uesb.prematricula.reports.model.dto.response.ProcessSummaryResponseDTO;
import br.edu.uesb.prematricula.reports.model.dto.response.StudentWithoutEnrollmentResponseDTO;
import br.edu.uesb.prematricula.reports.service.ReportService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/processes/{processId}/class-demand")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClassDemandResponseDTO>> findClassDemand(
            @PathVariable UUID processId) {

        return ResponseEntity.ok(
                reportService.findClassDemand(processId));
    }

    @GetMapping("/process-classes/{processClassId}/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrolledStudentResponseDTO>> findStudentsByProcessClass(
            @PathVariable UUID processClassId) {

        return ResponseEntity.ok(
                reportService.findStudentsByProcessClass(
                        processClassId));
    }

    @GetMapping("/processes/{processId}/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProcessSummaryResponseDTO> findProcessSummary(
            @PathVariable UUID processId) {

        return ResponseEntity.ok(
                reportService.findProcessSummary(processId));
    }

    @GetMapping("/processes/{processId}/students-without-enrollment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentWithoutEnrollmentResponseDTO>> findStudentsWithoutEnrollment(
            @PathVariable UUID processId) {

        return ResponseEntity.ok(
                reportService.findStudentsWithoutEnrollment(
                        processId));
    }

}
