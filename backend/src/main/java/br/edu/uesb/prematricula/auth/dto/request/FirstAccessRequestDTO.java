package br.edu.uesb.prematricula.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record FirstAccessRequestDTO(

    @NotBlank
    String registrationNumber,

    @NotBlank
    @Email
    String email
    
) { }
