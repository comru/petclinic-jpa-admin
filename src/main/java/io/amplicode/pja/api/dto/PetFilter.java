package io.amplicode.pja.api.dto;

import io.amplicode.pja.model.Pet;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

import static io.amplicode.pja.repository.PetRepository.Specifications.*;

public record PetFilter(
        String q,
        Integer ownerId,
        String ownerFirstName,
        String ownerLastName,
        LocalDate birthDateGreaterThan,
        LocalDate birthDateLessThan
) {

    public Specification<Pet> toSpecification() {
        return Specification.where(nameContainsIgnoreCase(q).or(typeNameContainsIgnoreCase(q)))
                .and(ownerIdEqual(ownerId))
                .and(ownerFirstNameContainsIgnoreCase(ownerFirstName))
                .and(ownerLastNameContainsIgnoreCase(ownerLastName))
                .and(birthDateGte(birthDateGreaterThan))
                .and(birthDateLte(birthDateLessThan));
    }
}


