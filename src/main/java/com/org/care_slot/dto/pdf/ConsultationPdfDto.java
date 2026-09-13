package com.org.care_slot.dto.pdf;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record ConsultationPdfDto(
        Long consultationId,
        Long patientId,
        Long visitId,
        String code,
        String medicalRecordNumber,
        String hospitalAdmissionNumber,
        String patientName,
        Integer patientAge,
        String gender,
        LocalDate treatedFromDate,
        LocalDate treatedToDate,
        String bedNumber,
        String roomNumber,
        String departmentName,
        String diagnosisText,
        LocalDateTime consultationTime,
        String chairpersonName,
        String secretaryName,
        String participantsText,
        String clinicalSummary,
        String conclusionText,
        String treatmentPlan
) {}
