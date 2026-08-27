package com.org.care_slot.dto.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCancelRequest {
    private String reason;
}
