package io.amplicode.pja.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "owners", indexes = {
        @Index(name = "idx_owners_last_name", columnList = "last_name")
})
public class Owner extends Person {
    @Column(name = "address")
    @NotBlank
    private String address;

    @Column(name = "city")
    @NotBlank
    private String city;

    @Column(name = "telephone")
    @NotBlank
    @Digits(fraction = 0, integer = 10)
    private String telephone;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    @OrderBy("name")
    @BatchSize(size = 50)
    private List<Pet> pets = new ArrayList<>();

}