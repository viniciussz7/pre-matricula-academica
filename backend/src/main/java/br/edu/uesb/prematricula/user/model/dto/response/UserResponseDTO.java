package br.edu.uesb.prematricula.user.model.dto.response;

import java.util.UUID;

import br.edu.uesb.prematricula.user.model.enums.UserRole;

public record UserResponseDTO(

    UUID id,

    String fullName,

    String email,

    UserRole role,

    boolean firstAccess,

    boolean active

) { }
