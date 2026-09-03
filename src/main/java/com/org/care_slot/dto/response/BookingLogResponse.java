package com.org.care_slot.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingLogResponse {
    private Long id;
    private Long appointmentId;
    private String bookingCode;
    private String previousStatus;
    private String newStatus;
    private String eventType;
    private String note;
    private String actor;
    private LocalDateTime createdAt;
}
