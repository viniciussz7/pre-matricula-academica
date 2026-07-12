package br.edu.uesb.prematricula.enrollmentprocess.model.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateEnrollmentProcessRequestDTO(

    @NotBlank String title,

    @NotNull LocalDateTime startDate,

    @NotNull LocalDateTime endDate,

    boolean active
) {

}
