package io.amplicode.pja.api.mapper;

import io.amplicode.pja.api.dto.PetDto;
import io.amplicode.pja.api.dto.PetMinimalDto;
import io.amplicode.pja.model.Pet;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface PetMapper {

    @Mapping(source = "ownerId", target = "owner.id")
    @Mapping(source = "typeId", target = "type.id")
    Pet toEntity(PetDto petDto);

    @InheritInverseConfiguration(name = "toEntity")
    PetDto toDto(Pet pet);

    PetMinimalDto toPetMinimal(Pet pet);

    @Mapping(source = "typeId", target = "type")
    @Mapping(source = "ownerId", target = "owner")
    void update(PetDto petDto, @MappingTarget Pet pet);
}
