package br.edu.uesb.prematricula.enrollment.model.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEnrollmentRequestDTO(

                @NotEmpty(message = "At least one class must be selected.") @Size(max = 7, message = "An enrollment cannot contain more than 7 classes.") List<@NotNull(message = "Class identifier cannot be null.") UUID> enrollmentProcessClassIds

) {
}