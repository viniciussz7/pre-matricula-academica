package br.edu.uesb.prematricula.enrollment.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EnrollmentResponseDTO(

        UUID id,

        UUID studentId,

        UUID enrollmentProcessId,

        String enrollmentProcessTitle,

        List<EnrollmentItemResponseDTO> items,

        int totalItems,

        boolean active,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}