package com.org.care_slot.dto.response;

import com.org.care_slot.enums.AppointmentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private String bookingCode;

    private Long patientProfileId;
    private String patientName;

    private Long doctorId;
    private String doctorName;
    private String clinicName;
    private String specialtyName;

    private Long slotId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String roomName;

    private String symptomNote;
    private BigDecimal consultationFee;
    private BigDecimal depositAmount;
    private AppointmentStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime checkedInAt;
}
