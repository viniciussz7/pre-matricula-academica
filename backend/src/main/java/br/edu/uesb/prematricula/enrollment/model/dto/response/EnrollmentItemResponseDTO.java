package br.edu.uesb.prematricula.enrollment.model.dto.response;

import java.util.UUID;

public record EnrollmentItemResponseDTO(

        UUID id,

        UUID enrollmentProcessClassId,

        UUID classGroupId,

        String classGroupCode,

        String classGroupName

) {
}