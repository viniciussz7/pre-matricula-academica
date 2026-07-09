package br.edu.uesb.prematricula.user.model.dto.request;

import br.edu.uesb.prematricula.user.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequestDTO(
    
    @NotBlank
    String fullName,

    @NotBlank
    @Email
    String email,

    @NotNull
    UserRole role

) { }
