package br.edu.uesb.prematricula.discipline.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DisciplineRequestDTO(
    
    @NotBlank(message = "O código é obrigatório")
    @Size(max = 20, message = "O código deve ter no máximo 20 caracteres")
    String code,

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
    String name,

    @NotNull(message = "A carga horária é obrigatória")
    @Min(value = 1, message = "A carga horária deve ser maior que zero")
    Integer workload,

    String prerequisites,

    Boolean active
) {}