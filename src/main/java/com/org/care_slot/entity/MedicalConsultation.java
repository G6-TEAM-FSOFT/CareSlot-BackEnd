package com.org.care_slot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_consultations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalConsultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;

    @Column(name = "medical_record_number", length = 50)
    private String medicalRecordNumber;

    @Column(name = "hospital_admission_number", length = 50)
    private String hospitalAdmissionNumber;

    @Column(name = "patient_name_snapshot", nullable = false, length = 100)
    private String patientNameSnapshot;

    @Column(name = "patient_age", nullable = false)
    private Integer patientAge;

    @Column(name = "patient_gender", nullable = false, length = 10)
    private String patientGender;

    @Column(name = "treated_from_date")
    private LocalDate treatedFromDate;

    @Column(name = "treated_to_date")
    private LocalDate treatedToDate;

    @Column(name = "bed_number", length = 20)
    private String bedNumber;

    @Column(name = "room_number", length = 20)
    private String roomNumber;

    @Column(name = "department_name", length = 100)
    private String departmentName;

    @Column(name = "diagnosis_text", columnDefinition = "TEXT")
    private String diagnosisText;

    @Column(name = "consultation_time", nullable = false)
    private LocalDateTime consultationTime;

    @Column(name = "chairperson_name", nullable = false, length = 100)
    private String chairpersonName;

    @Column(name = "secretary_name", nullable = false, length = 100)
    private String secretaryName;

    @Column(name = "participants_text", columnDefinition = "TEXT")
    private String participantsText;

    @Column(name = "clinical_summary", columnDefinition = "TEXT")
    private String clinicalSummary;

    @Column(name = "conclusion_text", columnDefinition = "TEXT")
    private String conclusionText;

    @Column(name = "treatment_plan", columnDefinition = "TEXT")
    private String treatmentPlan;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "FINAL";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (status == null) status = "FINAL";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
