package br.edu.uesb.prematricula.classgroup.model.dto.response;

import java.util.UUID;

public record ClassGroupResponseDTO(
    UUID id,
    String code,
    String name,
    UUID disciplineId,
    UUID academicPeriodId,
    Integer vacancies,
    Boolean allowOversubscription,
    Boolean active
) {}
