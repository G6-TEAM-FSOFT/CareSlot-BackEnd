package com.org.care_slot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prescriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Prescription extends BaseEntity {

    @Column(name = "prescription_code", nullable = false, unique = true, length = 50)
    private String prescriptionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encounter_id", nullable = false)
    private Encounter encounter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescribed_by", nullable = false)
    private Doctor prescribedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entered_by", nullable = false)
    private User enteredBy;

    @Column(name = "diagnosis_note", columnDefinition = "TEXT")
    private String diagnosisNote;

    @Column(name = "total_estimated_cost", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalEstimatedCost = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "FINAL";

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PrescriptionItem> items = new ArrayList<>();
}
