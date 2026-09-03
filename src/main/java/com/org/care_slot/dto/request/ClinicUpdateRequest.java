package com.org.care_slot.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicUpdateRequest {

    @NotBlank(message = "Clinic name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phone;
    private String description;
    private String status;
}
