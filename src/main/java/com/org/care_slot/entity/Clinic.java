package com.org.care_slot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "clinics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = { "doctors", "users", "specialties" })
@EqualsAndHashCode(callSuper = true, exclude = { "doctors", "users", "specialties" })
public class Clinic extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "ACTIVE";

    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Doctor> doctors = new ArrayList<>();

    @OneToMany(mappedBy = "clinic")
    @Builder.Default
    private List<User> users = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "clinic_specialties", joinColumns = @JoinColumn(name = "clinic_id"), inverseJoinColumns = @JoinColumn(name = "specialty_id"))
    @Builder.Default
    private Set<Specialty> specialties = new HashSet<>();
}
