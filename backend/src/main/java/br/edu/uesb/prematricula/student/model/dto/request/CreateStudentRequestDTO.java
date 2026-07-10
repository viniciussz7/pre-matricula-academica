package br.edu.uesb.prematricula.student.model.dto.request;

import jakarta.validation.constraints.*;

public record CreateStudentRequestDTO(

    @NotBlank
    String fullName,

    @NotBlank
    @Email
    String email,

    @NotBlank
    String registrationNumber

) { }