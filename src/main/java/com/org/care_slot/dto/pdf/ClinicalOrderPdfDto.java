package com.org.care_slot.dto.pdf;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ClinicalOrderPdfDto(
        Long orderId,
        Long patientId,
        Long visitId,
        String orderCode,
        Integer orderRound,
        String patientName,
        Integer age,
        String gender,
        String phone,
        String address,
        String clinicName,
        String doctorName,
        LocalDateTime createdDate,
        List<ServiceRequestPdfItemDto> services
) {
    @Builder
    public record ServiceRequestPdfItemDto(
            Integer index,
            String serviceCode,
            String serviceName,
            String category,
            String note,
            String status
    ) {}
}
