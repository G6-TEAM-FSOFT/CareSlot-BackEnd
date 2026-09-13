package com.org.care_slot.dto.pdf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitSummaryPdfDto {
    private Long visitId;
    private Long patientId;
    private String visitCode;
    private String patientName;
    private int age;
    private String gender;
    private String phone;
    private String address;
    private String clinicName;
    private String departmentName;
    private String doctorName;
    private LocalDateTime checkedInAt;
    private LocalDateTime completedAt;

    private String vitalSignsText;
    private String clinicalNotesText;
    private String diagnosisText;
    private String treatmentPlan;
    private String advice;

    private List<ServiceSummaryDto> serviceResults;
    private List<PrescriptionItemSummaryDto> prescriptionItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceSummaryDto {
        private int index;
        private String serviceName;
        private String category;
        private String findings;
        private String conclusion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionItemSummaryDto {
        private int index;
        private String drugName;
        private String dosage;
        private String usageInstruction;
        private int quantity;
        private String unit;
    }
}
