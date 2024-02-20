package io.amplicode.pja.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@FieldNameConstants
@Getter
@Setter
@MappedSuperclass
public class NamedEntity extends BaseEntity {
    @Column(name = "name")
    private String name;

    protected static class Fields extends BaseEntity.Fields {}
}