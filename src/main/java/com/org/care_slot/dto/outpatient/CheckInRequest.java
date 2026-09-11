package com.org.care_slot.dto.outpatient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequest {
    @NotNull
    private Long appointmentId;
    private Long replacementSlotId;
    @Size(max = 1000)
    private String reason;
}
