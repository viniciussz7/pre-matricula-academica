package br.edu.uesb.prematricula.enrollmentprocessclasses.model.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateEnrollmentProcessClassRequestDTO(

    @NotNull
    UUID enrollmentProcessId,

    @NotNull
    UUID classGroupId

) { }