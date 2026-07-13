package br.edu.uesb.prematricula.reports.model.dto.response;

import java.util.UUID;

public record StudentWithoutEnrollmentResponseDTO(

        UUID studentId,

        UUID userId,

        String registrationNumber,

        String studentName,

        String studentEmail,

        Boolean firstAccess) {

}
