package br.edu.uesb.prematricula.academicperiod.model.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record AcademicPeriodResponseDTO(
    UUID id,
    String code,
    String name,
    LocalDate startDate,
    LocalDate endDate,
    Boolean active
) {}