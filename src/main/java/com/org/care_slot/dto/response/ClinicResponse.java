package com.org.care_slot.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicResponse {
    private Long id;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phone;
    private String description;
    private String status;

    // Derived fields for US-13 (T-134 / T-136)
    private List<String> specialtyNames;
    private BigDecimal minConsultationFee;
    private BigDecimal maxConsultationFee;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime earliestAvailableSlot;
    private Double distanceKm;
}
