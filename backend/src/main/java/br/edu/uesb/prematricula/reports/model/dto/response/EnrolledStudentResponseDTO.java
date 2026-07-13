package br.edu.uesb.prematricula.reports.model.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record EnrolledStudentResponseDTO(

        UUID enrollmentItemId,

        UUID enrollmentId,

        UUID studentId,

        String registrationNumber,

        String studentName,

        String studentEmail,

        LocalDateTime selectedAt) {

}
