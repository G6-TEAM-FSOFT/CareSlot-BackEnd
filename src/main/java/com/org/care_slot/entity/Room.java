package com.org.care_slot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Room extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "room_number", nullable = false, length = 50)
    private String roomNumber;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "room_type", nullable = false, length = 50)
    private String roomType; // CONSULTATION, LAB_COLLECTION, ULTRASOUND, XRAY, CT, CASHIER

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "ACTIVE";
}
