package io.amplicode.pja.rasupport;

import io.amplicode.pja.model.Pet;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Component
public class SearchHelper {

    @PersistenceContext
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public <R, E> Page<R> findAll(Class<E> entityClass, Class<R> resultClass, Specification<E> specification, Pageable pageable) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<R> query = builder.createQuery(resultClass);
        Root<E> root = query.from(entityClass);

        List<Selection<?>> selections = new ArrayList<>();
        for (Field field : resultClass.getDeclaredFields()) {
            selections.add(root.get(field.getName()));
        }
        query.multiselect(selections).where(specification.toPredicate(root, query, builder));

        pageable.getSort().forEach(order -> {
            Path<Object> orderPath = root.get(order.getProperty());
            query.orderBy(order.getDirection() == Sort.Direction.ASC
                    ? builder.asc(orderPath)
                    : builder.desc(orderPath));
        });

        List<R> result = entityManager
                .createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        CriteriaQuery<Long> countQuery = builder.createQuery(Long.class);
        Root<Pet> booksRootCount = countQuery.from(Pet.class);
        countQuery.select(builder.count(booksRootCount))
                .where(specification.toPredicate(root, query, builder));
        Long count = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(result, pageable, count);
    }
}
