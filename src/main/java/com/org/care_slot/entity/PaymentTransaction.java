package com.org.care_slot.entity;

import com.org.care_slot.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"appointment"})
@EqualsAndHashCode(callSuper = true, exclude = {"appointment"})
public class PaymentTransaction extends BaseEntity {

    @Column(name = "txn_ref", nullable = false, unique = true, length = 100)
    private String txnRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_provider", length = 50)
    private String paymentProvider;

    @Column(name = "bank_code", length = 20)
    private String bankCode;

    @Column(name = "transaction_no", length = 100)
    private String transactionNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "response_code", length = 20)
    private String responseCode;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;
}
