package br.edu.uesb.prematricula.student.model.dto.response;

import java.util.UUID;

public record StudentResponseDTO(

    UUID id,

    UUID userId,

    String fullName,

    String email,

    String registrationNumber,

    boolean active

) { }