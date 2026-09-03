package com.org.care_slot.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorUpdateRequest {

    private Long specialtyId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    private String title;
    private String bio;
    private String avatarUrl;

    @DecimalMin(value = "0.0", message = "Consultation fee cannot be negative")
    private BigDecimal consultationFee;

    private String status;
}
