package io.amplicode.pja.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.amplicode.pja.api.dto.OwnerFilter;
import io.amplicode.pja.api.mapper.OwnerMapper;
import io.amplicode.pja.model.BaseEntity;
import io.amplicode.pja.model.Owner;
import io.amplicode.pja.api.dto.OwnerDto;
import io.amplicode.pja.rasupport.RaPatchUtil;
import io.amplicode.pja.repository.OwnerRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/rest/owners")
@RequiredArgsConstructor
public class OwnerResource {

    private final OwnerRepository repository;
    private final OwnerMapper mapper;
    private final Validator validator;
    private final ObjectMapper objectMapper;
    private final RaPatchUtil raPatchUtil;

    @GetMapping
    public Page<OwnerDto> getList(@ModelAttribute OwnerFilter filter, Pageable pageable) {
        Specification<Owner> specification = filter.toSpecification();
        Page<Owner> page = repository.findAll(specification, pageable);
        return page.map(mapper::toDto);
    }


    @GetMapping("/{id}")
    public OwnerDto getOne(@PathVariable("id") Long id) {
        Optional<OwnerDto> optional = repository.findById(id).map(mapper::toDto);
        return optional.orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
    }

    @GetMapping("/by-ids")
    public List<OwnerDto> getMany(@RequestParam("ids") List<Long> ids) {
        return repository.findWithPetsByIdIn(ids)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @PostMapping
    public OwnerDto create(@RequestBody @Valid OwnerDto ownerDto) {
        if (ownerDto.getId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A new owner cannot already have an ID");
        }
        var entity = mapper.toEntity(ownerDto);
        var resultEntity = repository.save(entity);
        return mapper.toDto(resultEntity);
    }

    @PutMapping("/{id}")
    public OwnerDto purePut(@PathVariable Long id,
                            @RequestBody @Valid OwnerDto updateOwnerDto) {
        Owner owner = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
        mapper.update(updateOwnerDto, owner);
        owner = repository.save(owner);
        return mapper.toDto(owner);
    }

    @PatchMapping("/by-dto/{id}")
    public OwnerDto patchByDto(@PathVariable Long id,
                               @RequestBody OwnerDto patchOwnerDto) throws BindException {
        Owner owner = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));

        OwnerDto ownerDto = mapper.toDto(owner);
        mapper.update(ownerDto, patchOwnerDto);
        validate(ownerDto);

        mapper.update(ownerDto, owner);
        owner = repository.save(owner);

        return mapper.toDto(owner);
    }

    @PatchMapping("/by-json/{id}")
    public OwnerDto patchByJsonNode(@PathVariable Long id,
                          @RequestBody JsonNode patchNode) throws BindException, IOException {
        Owner owner = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));

        OwnerDto ownerDto = mapper.toDto(owner);

        //comment
        patchAndValidate(ownerDto, patchNode);

        mapper.update(ownerDto, owner);
        owner = repository.save(owner);

        return mapper.toDto(owner);
    }

    @PatchMapping("/by-starter/{id}")
    public OwnerDto patchByStarter(@PathVariable Long id,
                                   @RequestBody String patchJson) throws BindException, IOException {
        Owner owner = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));

        OwnerDto ownerDto = mapper.toDto(owner);
        ownerDto = raPatchUtil.patchAndValidate(ownerDto, patchJson);

        mapper.update(ownerDto, owner);
        owner = repository.save(owner);

        return mapper.toDto(owner);
    }

    /**
     *
     * @param data
     * @param patchNode
     * @throws BindException
     * @throws IOException
     */
    private void patchAndValidate(OwnerDto data, JsonNode patchNode) throws BindException, IOException {
        ObjectReader objectReader = objectMapper.readerForUpdating(data);
        objectReader.readValue(patchNode);
        validate(data);
    }

    public void validate(Object target) throws BindException {
//        Errors errors = validator.validateObject(target);
//        if (errors.hasErrors()) {
//            throw new BindException(target, target.getClass().getName());
//        }

        DataBinder dataBinder = new DataBinder(target);
        dataBinder.setValidator(validator);
        dataBinder.validate();
        BindingResult bindResult = dataBinder.getBindingResult();
        if (bindResult.hasErrors()) {
//            throw new MethodArgumentNotValidException(null, bindResult);
            throw new BindException(bindResult);
        }
    }

    @PutMapping
    public List<Long> updateMany(@RequestParam List<Long> ids, @RequestBody OwnerDto ownerDto) {
        List<Owner> toUpdateOwners = repository.findAllById(ids);
        for (Owner owner : toUpdateOwners) {
            mapper.update(ownerDto, owner);
        }
        repository.saveAll(toUpdateOwners);
        return toUpdateOwners.stream().map(BaseEntity::getId).toList();
    }

    @DeleteMapping("/{id}")
    public OwnerDto delete(@PathVariable("id") Long id) {
        OwnerDto ownerDto = repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));

        repository.deleteById(id);

        return ownerDto;
    }

    @DeleteMapping
    public List<Long> deleteMany(@RequestParam List<Long> ids) {
        List<Owner> existingOwners = repository.findAllById(ids);

        repository.deleteAllById(ids);

        return existingOwners.stream().map(BaseEntity::getId).toList();
    }
}