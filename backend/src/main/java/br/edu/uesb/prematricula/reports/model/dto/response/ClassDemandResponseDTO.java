package br.edu.uesb.prematricula.reports.model.dto.response;

import java.util.UUID;

public record ClassDemandResponseDTO(

        UUID enrollmentProcessClassId,

        UUID classGroupId,

        String classCode,

        String className,

        String disciplineCode,

        String disciplineName,

        Integer vacancies,

        Boolean allowOversubscription,

        Long enrolledStudents,

        Integer remainingVacancies,

        Double occupancyPercentage

) {
}
