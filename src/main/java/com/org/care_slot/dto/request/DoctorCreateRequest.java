package com.org.care_slot.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorCreateRequest {

    @NotNull(message = "Specialty ID is required")
    private Long specialtyId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required for doctor login account")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;
    private String password;

    private String title;
    private String bio;
    private String avatarUrl;

    @NotNull(message = "Consultation fee is required")
    @DecimalMin(value = "0.0", message = "Consultation fee cannot be negative")
    private BigDecimal consultationFee;

    private String status;
}
