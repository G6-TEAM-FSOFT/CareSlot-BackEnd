package com.org.care_slot.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReassignDoctorRequest {
    @NotNull(message = "Vui lòng chọn slot bác sĩ thay thế")
    private Long replacementSlotId;
    private String reason;
}
