package io.amplicode.pja.repository;

import io.amplicode.pja.model.Pet;
import io.amplicode.pja.model.Pet.PetFields;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long>, JpaSpecificationExecutor<Pet> {

    <T> List<T> findByIdIn(Collection<Long> ids, Class<T> projection);

    @Transactional
    @Modifying
    void deleteByNameInIgnoreCase(Collection<String> names);

    class Specifications {
        public static Specification<Pet> nameContainsIgnoreCase(String value) {
            return ((root, query, cb) -> StringUtils.hasText(value)
                    ? cb.like(cb.lower(root.get(PetFields.name)), "%" + value.toLowerCase() + "%")
                    : null);
        }

        public static Specification<Pet> typeNameContainsIgnoreCase(String value) {
            return ((root, query, cb) -> StringUtils.hasText(value)
                    ? cb.like(cb.lower(root.get(PetFields.type).get("name")), "%" + value.toLowerCase() + "%")
                    : null);
        }

        public static Specification<Pet> ownerIdEqual(Integer ownerId) {
            return ((root, query, cb) -> ownerId != null
                    ? cb.equal(root.get(PetFields.owner).get("id"), ownerId)
                    : null);
        }

        public static Specification<Pet> ownerFirstNameContainsIgnoreCase(String ownerFirstName) {
            return ((root, query, cb) -> StringUtils.hasText(ownerFirstName)
                    ? cb.like(cb.lower(root.get(PetFields.owner).get("firstName")), "%" + ownerFirstName.toLowerCase() + "%")
                    : null);
        }

        public static Specification<Pet> ownerLastNameContainsIgnoreCase(String ownerLastName) {
            return ((root, query, cb) -> StringUtils.hasText(ownerLastName)
                    ? cb.like(cb.lower(root.get(PetFields.owner).get("lastName")), "%" + ownerLastName.toLowerCase() + "%")
                    : null);
        }

        public static Specification<Pet> birthDateLte(LocalDate birthDateLessThan) {
            return ((root, query, cb) -> birthDateLessThan != null
                    ? cb.lessThanOrEqualTo(root.get(PetFields.birthDate), birthDateLessThan)
                    : null);
        }

        public static Specification<Pet> birthDateGte(LocalDate birthDateGreaterThan) {
            return ((root, query, cb) -> birthDateGreaterThan != null
                    ? cb.greaterThanOrEqualTo(root.get(PetFields.birthDate), birthDateGreaterThan)
                    : null);
        }
    }
}