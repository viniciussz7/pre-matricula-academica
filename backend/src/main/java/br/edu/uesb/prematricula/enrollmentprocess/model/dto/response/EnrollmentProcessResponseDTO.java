package br.edu.uesb.prematricula.enrollmentprocess.model.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import br.edu.uesb.prematricula.enrollmentprocess.model.enums.EnrollmentProcessStatus;

public record EnrollmentProcessResponseDTO(

    UUID id,

    String title,

    UUID academicPeriodId,

    String academicPeriodCode,

    String academicPeriodName,

    LocalDateTime startDate,

    LocalDateTime endDate,

    EnrollmentProcessStatus status,

    boolean active
    
) {
    
}
