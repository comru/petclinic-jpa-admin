package io.amplicode.pja.api.dto;

import io.amplicode.pja.model.Pet;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

public record PetFilter(
        String q,
        Integer ownerId,
        String ownerFirstName,
        String ownerLastName,
        LocalDate birthDateGreaterThan,
        LocalDate birthDateLessThan
) {

    public Specification<Pet> toSpecification() {
        return Specification.where(nameSpec().or(typeNameSpec()))
                .and(ownerIdSpec())
                .and(ownerFirstNameSpec())
                .and(ownerLastNameSpec())
                .and(birthDateGreaterThanOrEqualSpec())
                .and(birthDateLessThanOrEqualSpec());
    }

    private Specification<Pet> nameSpec() {
        return ((root, query, cb) -> StringUtils.hasText(q)
                ? cb.like(cb.lower(root.get("name")), "%" + q.toLowerCase() + "%")
                : null);
    }

    private Specification<Pet> typeNameSpec() {
        return ((root, query, cb) -> StringUtils.hasText(q)
                ? cb.like(cb.lower(root.get("type").get("name")), "%" + q.toLowerCase() + "%")
                : null);
    }

    private Specification<Pet> ownerIdSpec() {
        return ((root, query, cb) -> ownerId != null
                ? cb.equal(root.get("owner").get("id"), ownerId)
                : null);
    }

    private Specification<Pet> ownerFirstNameSpec() {
        return ((root, query, cb) -> StringUtils.hasText(ownerFirstName)
                ? cb.like(cb.lower(root.get("owner").get("firstName")), "%" + ownerFirstName.toLowerCase() + "%")
                : null);
    }

    private Specification<Pet> ownerLastNameSpec() {
        return ((root, query, cb) -> StringUtils.hasText(ownerLastName)
                ? cb.like(cb.lower(root.get("owner").get("lastName")), "%" + ownerLastName.toLowerCase() + "%")
                : null);
    }

    private Specification<Pet> birthDateLessThanOrEqualSpec() {
        return ((root, query, cb) -> birthDateLessThan != null
                ? cb.lessThanOrEqualTo(root.get("birthDate"), birthDateLessThan)
                : null);
    }

    private Specification<Pet> birthDateGreaterThanOrEqualSpec() {
        return ((root, query, cb) -> birthDateGreaterThan != null
                ? cb.greaterThanOrEqualTo(root.get("birthDate"), birthDateGreaterThan)
                : null);
    }
}


