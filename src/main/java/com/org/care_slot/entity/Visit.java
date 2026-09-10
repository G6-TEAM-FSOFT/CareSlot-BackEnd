package com.org.care_slot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "visits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Visit extends BaseEntity {

    @Column(name = "visit_code", nullable = false, unique = true, length = 50)
    private String visitCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_profile_id", nullable = false)
    private PatientProfile patientProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_doctor_id", nullable = false)
    private Doctor primaryDoctor;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "ACTIVE"; // CHECKED_IN, ACTIVE, COMPLETED, REFERRED, ADMITTED, CANCELLED

    @Column(name = "checked_in_at", nullable = false)
    private LocalDateTime checkedInAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
