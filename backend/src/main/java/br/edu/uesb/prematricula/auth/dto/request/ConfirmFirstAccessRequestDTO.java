package br.edu.uesb.prematricula.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfirmFirstAccessRequestDTO(

    @NotBlank
    String token,

    @NotBlank
    @Size(min = 8, max = 100)
    String password,

    @NotBlank
    String confirmPassword
) { }
