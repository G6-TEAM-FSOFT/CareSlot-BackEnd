package com.org.care_slot.dto.pdf;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record ExaminationPdfDto(
        Long visitId,
        Long patientId,
        String visitCode,
        String patientName,
        LocalDate dateOfBirth,
        Integer age,
        String gender,
        String phone,
        String address,
        String clinicName,
        String departmentName,
        String doctorName,
        LocalDateTime visitDate,
        String chiefComplaint,
        String vitalSignsText,
        String diagnosisText,
        String treatmentPlan,
        String advice,
        String followUpDate
) {}
