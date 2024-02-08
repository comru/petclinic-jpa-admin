package io.amplicode.pja.api.dto;

import io.amplicode.pja.model.Owner;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public record OwnerFilter(String q,
                          String address,
                          String city) {

    public Specification<Owner> toSpecification() {
        return Specification.where(firstNameSpec().or(lastNameSpec()))
                .and(addressSpec())
                .and(citySpec());
    }

    private Specification<Owner> addressSpec() {
        return ((root, query, cb) -> StringUtils.hasText(address)
                ? cb.like(cb.lower(root.get("address")), "%" + address.toLowerCase() + "%")
                : null);
    }

    private Specification<Owner> citySpec() {
        return ((root, query, cb) -> StringUtils.hasText(city)
                ? cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%")
                : null);
    }

    private Specification<Owner> firstNameSpec() {
        return ((root, query, cb) -> StringUtils.hasText(q)
                ? cb.like(cb.lower(root.get("firstName")), "%" + q.toLowerCase() + "%")
                : null);
    }

    private Specification<Owner> lastNameSpec() {
        return ((root, query, cb) -> StringUtils.hasText(q)
                ? cb.like(cb.lower(root.get("lastName")), "%" + q.toLowerCase() + "%")
                : null);
    }
}
