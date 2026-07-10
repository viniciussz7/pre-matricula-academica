package br.edu.uesb.prematricula.student.model.dto.request;

import jakarta.validation.constraints.*;

public record UpdateStudentRequestDTO(

    @NotBlank
    String fullName,

    @NotBlank
    @Email
    String email

) { }