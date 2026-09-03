package com.org.care_slot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "booking_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"appointment"})
@EqualsAndHashCode(callSuper = true, exclude = {"appointment"})
public class BookingLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;

    @Column(name = "booking_code", nullable = false, length = 50)
    private String bookingCode;

    @Column(name = "previous_status", length = 30)
    private String previousStatus;

    @Column(name = "new_status", nullable = false, length = 30)
    private String newStatus;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "actor", length = 100)
    private String actor;
}
