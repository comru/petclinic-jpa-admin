package io.amplicode.pja.api.dto;

import io.amplicode.pja.model.Pet;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record PetFilter(
        String q,
        Integer ownerId,
        String ownerFirstName,
        String ownerLastName,
        LocalDate birthDateGreaterThan,
        LocalDate birthDateLessThan
) {

    public Specification<Pet> toSpecificationImprove() {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(q)) {
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), "%" + q.toLowerCase() + "%"),
                        cb.like(cb.lower(root.get("type").get("name")), "%" + q.toLowerCase() + "%")
                ));
            }
            if (ownerId != null) {
                predicates.add(cb.equal(root.get("owner").get("id"), ownerId));
            }
            if (StringUtils.hasText(ownerFirstName)) {
                predicates.add(cb.like(cb.lower(root.get("owner").get("firstName")), "%" + ownerFirstName.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(ownerLastName)) {
                predicates.add(cb.like(cb.lower(root.get("owner").get("lastName")), "%" + ownerLastName.toLowerCase() + "%"));;
            }
            if (birthDateLessThan != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("birthDate"), birthDateLessThan));
            }
            if (birthDateGreaterThan != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("birthDate"), birthDateGreaterThan));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

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
                ? cb.like(cb.lower(root.join("type").get("name")), "%" + q.toLowerCase() + "%")
                : null);
    }

    private Specification<Pet> ownerIdSpec() {
        return ((root, query, cb) -> ownerId != null
                ? cb.equal(root.join("owner", JoinType.LEFT).get("id"), ownerId)
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


