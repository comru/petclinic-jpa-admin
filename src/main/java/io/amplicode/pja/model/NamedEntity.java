package io.amplicode.pja.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class NamedEntity extends BaseEntity {
    @Column(name = "name")
    private String name;

}