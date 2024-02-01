package io.amplicode.pja.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;

/**
 * DTO for {@link io.amplicode.pja.model.Pet}
 */
public record PetOneDto(Long id,
                        @NotNull String name,
                        @NotNull Long typeId
) {
}
