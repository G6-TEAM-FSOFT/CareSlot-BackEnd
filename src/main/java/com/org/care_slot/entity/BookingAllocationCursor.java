package com.org.care_slot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "booking_allocation_cursors", uniqueConstraints =
        @UniqueConstraint(columnNames = {"clinic_id", "specialty_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class BookingAllocationCursor extends BaseEntity {
    @Column(name = "clinic_id", nullable = false)
    private Long clinicId;
    @Column(name = "specialty_id", nullable = false)
    private Long specialtyId;
    @Column(name = "last_doctor_id")
    private Long lastDoctorId;
}
