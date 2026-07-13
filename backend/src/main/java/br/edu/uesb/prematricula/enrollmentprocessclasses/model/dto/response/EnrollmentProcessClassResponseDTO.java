package br.edu.uesb.prematricula.enrollmentprocessclasses.model.dto.response;

import java.util.UUID;

public record EnrollmentProcessClassResponseDTO(

    UUID id,

    UUID enrollmentProcessId,

    String enrollmentProcessTitle,

    UUID classGroupId,

    String classGroupCode,

    String classGroupName,

    boolean active

) { }