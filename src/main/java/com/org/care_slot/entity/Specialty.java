package com.org.care_slot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "specialties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"clinics", "doctors"})
@EqualsAndHashCode(callSuper = true, exclude = {"clinics", "doctors"})
public class Specialty extends BaseEntity {

    @ManyToMany(mappedBy = "specialties")
    @Builder.Default
    private Set<Clinic> clinics = new HashSet<>();

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "ACTIVE";

    @OneToMany(mappedBy = "specialty")
    @Builder.Default
    private List<Doctor> doctors = new ArrayList<>();
}


