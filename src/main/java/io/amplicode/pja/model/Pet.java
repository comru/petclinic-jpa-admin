package io.amplicode.pja.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;


@FieldNameConstants(innerTypeName = "PetFields")
@Getter
@Setter
@Entity
@Table(name = "pets", indexes = {
        @Index(name = "idx_pets_name", columnList = "name"),
        @Index(name = "idx_pets_owner_id", columnList = "owner_id")
})
public class Pet extends NamedEntity {

    @Column(name = "birth_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private PetType type;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.REMOVE, CascadeType.DETACH}, mappedBy = "pet")
    @OrderBy("visit_date ASC")
    private Set<Visit> visits = new LinkedHashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Owner owner;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "user_data")
    private Map<String, Object> userData;

    public static class PetFields extends Fields {}
}