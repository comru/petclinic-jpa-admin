package io.amplicode.pja.api.mapper;

import io.amplicode.pja.api.dto.OwnerDto;
import io.amplicode.pja.model.Owner;
import org.mapstruct.*;

@Mapper(config = MapStructConfig.class)
public interface OwnerMapper {

    Owner toEntity(OwnerDto ownerDto);

    @Mapping(target = "petIds", source = "pets")
    OwnerDto toDto(Owner owner);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(OwnerDto ownerDto, @MappingTarget Owner owner);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(@MappingTarget OwnerDto updateOwnerDto, OwnerDto currentOwnerDto);

}