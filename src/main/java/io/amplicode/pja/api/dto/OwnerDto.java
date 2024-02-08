package io.amplicode.pja.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.amplicode.pja.model.Owner;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link Owner}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class OwnerDto implements Serializable {
    Long id;
    @NotBlank String firstName;
    @NotBlank String lastName;
    @NotBlank String address;
    @NotBlank String city;
    @Digits(integer = 10, fraction = 0) @NotBlank String telephone;
    List<Long> petIds;
}