package io.amplicode.pja.api.mapper;

import io.amplicode.pja.api.dto.PetDto;
import io.amplicode.pja.api.dto.PetMinimalDto;
import io.amplicode.pja.model.Owner;
import io.amplicode.pja.model.Pet;
import io.amplicode.pja.model.PetType;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
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

    default Owner createOwner(Long ownerId) {
        if (ownerId == null) {
            return null;
        }
        Owner owner = new Owner();
        owner.setId(ownerId);
        return owner;
    }

    default PetType createPetType(Long typeId) {
        if (typeId == null) {
            return null;
        }
        PetType petType = new PetType();
        petType.setId(typeId);
        return petType;
    }
}
