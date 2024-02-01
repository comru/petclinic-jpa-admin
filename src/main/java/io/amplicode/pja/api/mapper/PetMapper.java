package io.amplicode.pja.api.mapper;

import io.amplicode.pja.api.dto.PetDto;
import io.amplicode.pja.api.dto.PetMinimalDto;
import io.amplicode.pja.model.Pet;
import io.amplicode.pja.rasupport.ReferenceMapper;
import io.amplicode.pja.rasupport.ResourceBaseMapper;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {OwnerMapper.class, PetTypeMapper.class, ReferenceMapper.class})
public interface PetMapper extends ResourceBaseMapper<Pet, Long> {

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
