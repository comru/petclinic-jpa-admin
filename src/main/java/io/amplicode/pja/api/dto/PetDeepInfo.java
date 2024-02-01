package io.amplicode.pja.api.dto;

import java.time.LocalDate;

/**
 * Projection for {@link io.amplicode.pja.model.Pet}
 */
public interface PetDeepInfo {
    Long getId();

    String getName();

    LocalDate getBirthDate();

    PetTypeInfo getType();

    OwnerInfo getOwner();

    /**
     * Projection for {@link io.amplicode.pja.model.PetType}
     */
    interface PetTypeInfo {
        Long getId();
    }

    /**
     * Projection for {@link io.amplicode.pja.model.Owner}
     */
    interface OwnerInfo {
        Long getId();
    }
}