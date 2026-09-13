package com.org.care_slot.dto.pdf;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ServiceResultPdfDto(
        Long resultId,
        Long patientId,
        Long visitId,
        String serviceCode,
        String serviceName,
        String category,
        String patientName,
        Integer age,
        String gender,
        String clinicName,
        String departmentName,
        String orderingDoctorName,
        String performingStaffName,
        LocalDateTime finalizedAt,
        String findings,
        String conclusion,
        String status
) {}
