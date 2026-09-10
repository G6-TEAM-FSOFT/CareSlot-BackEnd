package com.org.care_slot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "service_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ServiceRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinical_order_id", nullable = false)
    private ClinicalOrder clinicalOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceCatalog serviceCatalog;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "ORDERED"; // ORDERED, IN_PROGRESS, COMPLETED, CANCELLED

    @OneToOne(mappedBy = "serviceRequest")
    private ServiceResult serviceResult;

    @OneToOne(mappedBy = "serviceRequest")
    private ServiceTask serviceTask;
}
