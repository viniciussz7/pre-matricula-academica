package br.edu.uesb.prematricula.reports.model.dto.response;

import java.util.UUID;

public record ProcessSummaryResponseDTO(

        UUID enrollmentProcessId,

        String enrollmentProcessTitle,

        long totalEnrollments,

        long activeEnrollments,

        long cancelledEnrollments,

        int totalClasses,

        long totalSelections,

        double averageClassesPerActiveEnrollment,

        long fullClasses,

        long oversubscribedClasses,

        MostDemandedClassResponseDTO mostDemandedClass

) {

}
