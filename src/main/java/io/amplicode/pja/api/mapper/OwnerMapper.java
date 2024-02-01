package io.amplicode.pja.api.mapper;

import io.amplicode.pja.model.Owner;
import io.amplicode.pja.rasupport.ReferenceMapper;
import io.amplicode.pja.rasupport.ResourceBaseMapper;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ReferenceMapper.class})
public interface OwnerMapper extends ResourceBaseMapper<Owner, Long> {

}