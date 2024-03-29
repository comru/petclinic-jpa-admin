package io.amplicode.pja.api;

import io.amplicode.pja.api.dto.BaseDto;
import io.amplicode.pja.api.dto.PetDto;
import io.amplicode.pja.api.dto.PetFilter;
import io.amplicode.pja.api.dto.PetMinimalDto;
import io.amplicode.pja.api.mapper.PetMapper;
import io.amplicode.pja.model.Pet;
import io.amplicode.pja.rasupport.RaPatchUtil;
import io.amplicode.pja.repository.PetRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/rest/pets")
public class PetResource {

    private final PetMapper petMapper;
    private final RaPatchUtil raPatchUtil;
    private final PetRepository petRepository;

    @GetMapping
    public Page<PetDto> getList(@ModelAttribute PetFilter filter, @PageableDefault(size = 15) Pageable pageable) {
        Specification<Pet> specification = filter.toSpecification();
        Page<Pet> page = petRepository.findAll(specification, pageable);
        return page.map(petMapper::toDto);

        //Correct load by projection
//        Page<PetDto> page = searchHelper.findAll(
//                Pet.class,
//                PetDto.class,
//                List.of("id", "name", "birthDate", "type.id", "owner.id"),
//                specification,
//                pageable);
//        return ResponseEntity.ok(page);
    }


    @GetMapping("/{id}")
    public PetDto getOne(@PathVariable("id") Long id) {
        //dynamic projection based class
//        Optional<PetDto> petOptional = petRepository.findById(id, PetDto.class);
//        return ResponseEntity.of(petOptional);

        //dynamic projection
//        Optional<PetDeepInfo> petOptio
//        nal = petRepository.findById(id, PetDeepInfo.class);
//        return ResponseEntity.of(petOptional);

        //при чем разный результат будет при загрузке ассоциаций с @Value и без @Value, а если у одной аннотации указан
        //@Value то вообще упадет)
//        Optional<PetFlatInfo> petOptional = petRepository.findById(id, PetFlatInfo.class);
//        return ResponseEntity.of(petOptional);

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

    @PutMapping("/{id}")
    public PetDto update(@PathVariable Long id, @RequestBody String petDtoPatch) {
        Pet pet = petRepository.findById(id).orElse(null);
        if (pet == null) {
            throw createEntityNotFoundException(id);
        }

        PetDto petDto = petMapper.toDto(pet);
        petDto = raPatchUtil.patchAndValidate(petDto, petDtoPatch);

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
            petDto = raPatchUtil.patchAndValidate(petDto, patchJson);

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
        //TODO case 1
//        Optional<PetDto> petDto = petRepository.findById(id)
//                .map(petMapper::toDto);
//
//        if (petDto.isPresent()) {
//            petRepository.deleteById(id);
//        }
//        return ResponseEntity.of(petDto);

        //TODO case 2
        PetDto petDto = petRepository.findById(id)
                .map(petMapper::toDto)
                .orElseThrow(() -> createEntityNotFoundException(id));

        petRepository.deleteAllByIdInBatch(List.of(id));
        return petDto;

        //TODO case 3
//        petRepository.deleteById(id);

        //TODO case 4
//        petRepository.deleteAllByIdInBatch(List.of(id));
//        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public List<Long> deleteMany(@RequestParam List<Long> ids) {
        //The ids of the deleted records (optional)
        //TODO case 1
//        List<Pet> existingEntities = petRepository.findAllById(ids);
//
//        petRepository.deleteAllById(ids);
//
//        return existingEntities.stream().map(e -> e.getId()).toList();

        //TODO case 2
        List<BaseDto> existingEntities = petRepository.findByIdIn(ids, BaseDto.class);
        List<Long> toDeleteIds = existingEntities.stream()
                .map(BaseDto::id)
                .toList();

        petRepository.deleteAllByIdInBatch(toDeleteIds);

        return toDeleteIds;

        //TODO case 3
//        petRepository.deleteAllById(ids);

        //TODO case 4
//        petRepository.deleteAllByIdInBatch(ids);
//        return ResponseEntity.ok().build();
    }

    private ResponseStatusException createEntityNotFoundException(Long id) {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id));
    }
}

