package com.org.care_slot.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "service_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ServiceResult extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id", nullable = false)
    private ServiceRequest serviceRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ResultTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entered_by", nullable = false)
    private User enteredBy;

    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private String status = "FINAL"; // DRAFT, FINAL

    @Column(name = "findings", columnDefinition = "TEXT")
    private String findings;

    @Column(name = "conclusion", columnDefinition = "TEXT")
    private String conclusion;

    @Column(name = "result_data", nullable = false, columnDefinition = "JSON")
    private String resultData;

    @Column(name = "finalized_at", nullable = false)
    @Builder.Default
    private LocalDateTime finalizedAt = LocalDateTime.now();
}
