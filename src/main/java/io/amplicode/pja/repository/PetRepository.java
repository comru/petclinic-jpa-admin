package io.amplicode.pja.repository;

import io.amplicode.pja.api.dto.PetDto;
import io.amplicode.pja.api.dto.PetMinimalDto;
import io.amplicode.pja.model.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long>, JpaSpecificationExecutor<Pet> {
    <T> Optional<T> findById(Long id, Class<T> projection);

    List<PetDto> findPetDtoById(Long id);

    <T> List<T> findByIdIn(Collection<Long> ids, Class<T> projection);

}