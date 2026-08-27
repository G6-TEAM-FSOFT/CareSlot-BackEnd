package com.org.care_slot.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"clinic", "specialty", "slots"})
@EqualsAndHashCode(callSuper = true, exclude = {"clinic", "specialty", "slots"})
public class Doctor extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 1000)
    private String avatarUrl;

    @Column(name = "consultation_fee", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal consultationFee = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "ACTIVE";

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    @Builder.Default
    private List<AppointmentSlot> slots = new ArrayList<>();
}
