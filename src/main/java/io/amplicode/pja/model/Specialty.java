package io.amplicode.pja.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "specialties", indexes = {
        @Index(name = "idx_specialties_name", columnList = "name")
})
public class Specialty extends NamedEntity {

    @ManyToMany(mappedBy = "specialties")
    private Set<Vet> vets = new LinkedHashSet<>();

}