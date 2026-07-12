package br.edu.uesb.prematricula.enrollmentprocess.model.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateEnrollmentProcessRequestDTO(

    @NotBlank
    String title,

    @NotNull
    UUID academicPeriodId,

    @NotNull
    LocalDateTime startDate,

    @NotNull
    LocalDateTime endDate
    
) {
    
}
