package com.org.care_slot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "visit_dispositions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class VisitDisposition extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false, unique = true)
    private Visit visit;

    @Column(name = "disposition_type", nullable = false, length = 50)
    private String dispositionType; // OUTPATIENT, REFERRED, ADMITTED

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "destination_facility", length = 255)
    private String destinationFacility;

    @Column(name = "destination_department", length = 255)
    private String destinationDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
}
