package br.edu.uesb.prematricula.admin.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateAdminRequestDTO(

    @NotBlank
    String fullName,

    @NotBlank
    @Email
    String email
    
) {}