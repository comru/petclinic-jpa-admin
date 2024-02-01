package io.amplicode.pja.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO for {@link io.amplicode.pja.model.Pet}
 */
public record PetDto(Long id,
                     @NotNull String name,
                     LocalDate birthDate,
                     @NotNull Long typeId,
                     @NotNull Long ownerId
) {
}
