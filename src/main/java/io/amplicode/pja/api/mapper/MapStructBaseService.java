package io.amplicode.pja.api.mapper;

import io.amplicode.pja.model.BaseEntity;
import lombok.RequiredArgsConstructor;
import org.mapstruct.TargetType;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MapStructBaseService {

    private final JpaContext jpaContext;

    public  <T extends BaseEntity> T createEntity(@Nullable Long id, @TargetType Class<T> entityClass) {
        if (id == null) {
            return null;
        }
        var entityManager = jpaContext.getEntityManagerByManagedType(entityClass);
        return entityManager.getReference(entityClass, id);
    }

    public List<Long> convertEntitiesToIds(List<? extends BaseEntity> entities) {
        return entities.stream().map(BaseEntity::getId).toList();
    }
}
