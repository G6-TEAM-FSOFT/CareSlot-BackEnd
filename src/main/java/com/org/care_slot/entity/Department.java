package com.org.care_slot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Department extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "dept_type", nullable = false, length = 50)
    @Builder.Default
    private String deptType = "CLINICAL"; // CLINICAL, LABORATORY, IMAGING, RECEPTION

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "ACTIVE";
}
