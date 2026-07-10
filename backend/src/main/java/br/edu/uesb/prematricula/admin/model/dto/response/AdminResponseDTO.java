package br.edu.uesb.prematricula.admin.model.dto.response;

import java.util.UUID;

public record AdminResponseDTO (

    UUID id,

    UUID userId,

    String fullName,

    String email,

    boolean active

) { }
