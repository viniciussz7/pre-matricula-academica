package br.edu.uesb.prematricula.academicperiod.model.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AcademicPeriodRequestDTO(
    @NotBlank(message = "O código é obrigatório")
    @Size(max = 20, message = "O código deve ter no máximo 20 caracteres")
    String code,

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    String name,

    @NotNull(message = "A data de início é obrigatória")
    LocalDate startDate,

    @NotNull(message = "A data de fim é obrigatória")
    LocalDate endDate,
    
    Boolean active
) {}
