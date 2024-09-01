package io.amplicode.pja.repository;

import io.amplicode.pja.model.Owner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface OwnerRepository extends JpaRepository<Owner, Long>, JpaSpecificationExecutor<Owner> {

    @Override
    Page<Owner> findAll(Specification<Owner> spec, Pageable pageable);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = "pets")
    @Override
    Optional<Owner> findById(Long aLong);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = "pets")
    List<Owner> findWithPetsByIdIn(List<Long> ids);
}