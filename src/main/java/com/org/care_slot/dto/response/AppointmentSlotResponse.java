package com.org.care_slot.dto.response;

import com.org.care_slot.enums.SlotStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentSlotResponse {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String roomName;
    private SlotStatus status;
}
