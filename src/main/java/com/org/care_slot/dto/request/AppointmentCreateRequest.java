package com.org.care_slot.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCreateRequest {

    @NotNull(message = "Patient profile ID is required")
    private Long patientProfileId;

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    private String symptomNote;
    private BigDecimal depositAmount;
}
