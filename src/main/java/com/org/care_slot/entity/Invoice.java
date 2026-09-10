package com.org.care_slot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Invoice extends BaseEntity {

    @Column(name = "invoice_code", nullable = false, unique = true, length = 50)
    private String invoiceCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id")
    private Visit visit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_profile_id", nullable = false)
    private PatientProfile patientProfile;

    @Column(name = "invoice_type", nullable = false, length = 50)
    private String invoiceType; // APPOINTMENT_DEPOSIT, CLINICAL_SERVICE, FINAL_SETTLEMENT

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING"; // PENDING, PAID, CANCELLED

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();
}
