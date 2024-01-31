package io.amplicode.pja.rasupport;

import org.mapstruct.Mapping;

public interface ResourceBaseMapper<E, ID> {
    @Mapping(target = "id", ignore = true)
    E toEntity(ID id);
}
