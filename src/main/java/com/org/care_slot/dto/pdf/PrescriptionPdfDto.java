package com.org.care_slot.dto.pdf;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PrescriptionPdfDto(
        Long prescriptionId,
        Long patientId,
        Long visitId,
        String prescriptionCode,
        String patientName,
        Integer age,
        String gender,
        String address,
        String clinicName,
        String doctorName,
        String diagnosisNote,
        LocalDateTime createdDate,
        BigDecimal totalEstimatedCost,
        List<PrescriptionItemPdfDto> items
) {
    @Builder
    public record PrescriptionItemPdfDto(
            Integer index,
            String drugName,
            String dosage,
            String usageInstruction,
            Integer quantity,
            String unit,
            BigDecimal unitPrice,
            BigDecimal amount
    ) {}
}
