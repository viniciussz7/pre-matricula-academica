package br.edu.uesb.prematricula.discipline.model.dto.response;

import java.util.UUID;

public record DisciplineResponseDTO(
    UUID id,
    String code,
    String name,
    Integer workload,
    String prerequisites,
    Boolean active
) {}
