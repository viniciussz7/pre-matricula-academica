package br.edu.uesb.prematricula.auth.dto.response;

public record AuthResponseDTO(
    String token,
    String role,
    Object user
) { }
