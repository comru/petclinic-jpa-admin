package io.amplicode.pja.api.mapper;

import io.amplicode.pja.api.dto.OwnerDto;
import io.amplicode.pja.model.Owner;
import io.amplicode.pja.model.Pet;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING)
public interface OwnerMapper {

    Owner toEntity(OwnerDto ownerDto);

    @Mapping(target = "petIds", source = "pets")
    OwnerDto toDto(Owner owner);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(OwnerDto ownerDto, @MappingTarget Owner owner);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget OwnerDto updateOwnerDto, OwnerDto currentOwnerDto);

    default List<Long> petsToPetIds(List<Pet> pets) {
        return pets.stream().map(Pet::getId).collect(Collectors.toList());
    }
}