package com.org.care_slot.entity;

import com.org.care_slot.enums.AppointmentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"patientProfile", "slot", "approvedBy"})
@EqualsAndHashCode(callSuper = true, exclude = {"patientProfile", "slot", "approvedBy"})
public class Appointment extends BaseEntity {

    @Column(name = "booking_code", nullable = false, unique = true, length = 50)
    private String bookingCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_profile_id", nullable = false)
    private PatientProfile patientProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private AppointmentSlot slot;

    @Column(name = "symptom_note", columnDefinition = "TEXT")
    private String symptomNote;

    @Column(name = "consultation_fee", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal consultationFee = BigDecimal.ZERO;

    @Column(name = "deposit_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private AppointmentStatus status = AppointmentStatus.PENDING_PAYMENT;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;
}
