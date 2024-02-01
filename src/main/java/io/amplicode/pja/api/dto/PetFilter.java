package io.amplicode.pja.api.dto;

import io.amplicode.pja.model.Pet;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

public record PetFilter(
        String q,
        Integer ownerId,
        LocalDate birthDateGreaterThan,
        LocalDate birthDateLessThan
) {
    public Specification<Pet> toSpecification() {
        return Specification.where(nameSpec().or(typeNameSpec()))
                .and(ownerIdSpec())
                .and(birthDateGreaterThanOrEqualSpec())
                .and(birthDateLessThanOrEqualSpec());
    }

    private Specification<Pet> nameSpec() {
        return ((root, query, cb) -> StringUtils.hasText(q)
                ? cb.like(cb.lower(root.get("name")), "%" + q.toLowerCase() + "%")
                : cb.conjunction());
    }

    private Specification<Pet> typeNameSpec() {
        return ((root, query, cb) -> StringUtils.hasText(q)
                ? cb.like(cb.lower(root.join("type").get("name")), "%" + q.toLowerCase() + "%")
                : cb.conjunction());
    }

    private Specification<Pet> ownerIdSpec() {
        //todo join type???
        return ((root, query, cb) -> ownerId != null
                ? cb.equal(root.join("owner", JoinType.LEFT).get("id"), ownerId)
                : cb.conjunction());
    }

    private Specification<Pet> birthDateLessThanOrEqualSpec() {
        return ((root, query, cb) -> birthDateLessThan != null
                ? cb.lessThanOrEqualTo(root.get("birthDate"), birthDateLessThan)
                : cb.conjunction());
    }

    private Specification<Pet> birthDateGreaterThanOrEqualSpec() {
        return ((root, query, cb) -> birthDateGreaterThan != null
                ? cb.greaterThanOrEqualTo(root.get("birthDate"), birthDateGreaterThan)
                : cb.conjunction());
    }
}


