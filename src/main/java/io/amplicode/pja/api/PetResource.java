package io.amplicode.pja.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.amplicode.pja.api.dto.BaseDto;
import io.amplicode.pja.api.dto.PetDto;
import io.amplicode.pja.api.dto.PetFilter;
import io.amplicode.pja.api.dto.PetMinimalDto;
import io.amplicode.pja.api.mapper.PetMapper;
import io.amplicode.pja.model.Pet;
import io.amplicode.pja.repository.PetRepository;
import io.amplicode.rautils.patch.ObjectPatcher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/rest/pets")
public class PetResource {

    private final PetMapper petMapper;
    private final PetRepository petRepository;
    private final ObjectPatcher objectPatcher;

    @GetMapping
    public PagedModel<PetDto> getList(@ParameterObject @ModelAttribute PetFilter filter,
                                @ParameterObject @PageableDefault(size = 15) Pageable pageable) {
        Specification<Pet> specification = filter.toSpecification();
        Page<Pet> page = petRepository.findAll(specification, pageable);
        return new PagedModel<>(page.map(petMapper::toDto));
    }


    @GetMapping("/{id}")
    public PetDto getOne(@PathVariable("id") Long id) {
        Optional<Pet> petOptional = petRepository.findById(id);
        return petOptional.map(petMapper::toDto)
                .orElseThrow(() -> createEntityNotFoundException(id));
    }

    @GetMapping("/by-ids")
    public List<PetMinimalDto> getMany(@RequestParam List<Long> ids) {
        return petRepository.findAllById(ids)
                .stream()
                .map(petMapper::toPetMinimal)
                .toList();
    }

    @PostMapping
    public PetDto create(@RequestBody @Valid PetDto petDto) {
        if (petDto.id() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A new pet cannot already have an ID");
        }
        Pet pet = petMapper.toEntity(petDto);
        pet = petRepository.save(pet);
        return petMapper.toDto(pet);
    }

    @PostMapping("/many")
    public List<PetDto> createMany(@RequestBody List<PetDto> petDtos) {
        if (petDtos.stream().anyMatch(dto -> dto.id() != null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A new pet cannot already have an ID");
        }
        List<Pet> pets = petDtos.stream()
                .map(petMapper::toEntity)
                .toList();
        return petRepository.saveAll(pets)
                .stream()
                .map(petMapper::toDto)
                .toList();
    }

    @PutMapping("/{id}")
    public PetDto update(@PathVariable Long id, @RequestBody JsonNode petDtoPatch) {
        Pet pet = petRepository.findById(id).orElse(null);
        if (pet == null) {
            throw createEntityNotFoundException(id);
        }

        PetDto petDto = petMapper.toDto(pet);
        petDto = objectPatcher.patchAndValidate(petDto, petDtoPatch);

        if (petDto.id() != null && !petDto.id().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid id");
        }

        petMapper.update(petDto, pet);
        pet = petRepository.save(pet);
        return petMapper.toDto(pet);
    }

    @PutMapping
    public List<Long> updateMany(@RequestParam List<Long> ids, @RequestBody String patchJson) {
        List<Pet> updatedEntities = new ArrayList<>();
        List<Pet> toUpdatePets = petRepository.findAllById(ids);

        for (Pet pet: toUpdatePets) {
            PetDto petDto = petMapper.toDto(pet);
            petDto = objectPatcher.patchAndValidate(petDto, patchJson);

            if (petDto.id() != null && !petDto.id().equals(pet.getId())) { // attempt to change entity id
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid id");
            }

            petMapper.update(petDto, pet);
            updatedEntities.add(pet);
        }

        petRepository.saveAll(updatedEntities);

        return updatedEntities.stream().map(Pet::getId).toList();
    }

    @DeleteMapping("/{id}")
    public PetDto delete(@PathVariable Long id) {
        PetDto petDto = petRepository.findById(id)
                .map(petMapper::toDto)
                .orElseThrow(() -> createEntityNotFoundException(id));

        petRepository.deleteAllByIdInBatch(List.of(id));
        return petDto;
    }

    @DeleteMapping
    public List<Long> deleteMany(@RequestParam List<Long> ids) {
        List<BaseDto> existingEntities = petRepository.findByIdIn(ids, BaseDto.class);
        List<Long> toDeleteIds = existingEntities.stream()
                .map(BaseDto::id)
                .toList();

        petRepository.deleteAllByIdInBatch(toDeleteIds);

        return toDeleteIds;
    }

    private ResponseStatusException createEntityNotFoundException(Long id) {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id));
    }
}

