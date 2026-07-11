package br.edu.uesb.prematricula.classgroup.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ClassGroupRequestDTO(
    
    @NotBlank(message = "O código é obrigatório")
    @Size(max = 20, message = "O código deve ter no máximo 20 caracteres")
    String code,

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
    String name,

    @NotNull(message = "O ID da disciplina é obrigatório")
    UUID disciplineId,

    @NotNull(message = "O ID do período letivo é obrigatório")
    UUID academicPeriodId,

    @NotNull(message = "O número de vagas é obrigatório")
    @Min(value = 1, message = "A turma deve ter pelo menos 1 vaga")
    Integer vacancies,

    @NotNull(message = "O indicador de ultrapassar limite é obrigatório")
    Boolean allowOversubscription,

    Boolean active
) {}