package io.amplicode.pja.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "types", indexes = {
        @Index(name = "idx_types_name", columnList = "name")
})
public class PetType extends NamedEntity {
}