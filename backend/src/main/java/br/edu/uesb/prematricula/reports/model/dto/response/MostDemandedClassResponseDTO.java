package br.edu.uesb.prematricula.reports.model.dto.response;

import java.util.UUID;

public record MostDemandedClassResponseDTO(

        UUID enrollmentProcessClassId,

        UUID classGroupId,

        String classCode,

        String className,

        Long enrolledStudents) {

}
