package com.org.care_slot.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.org.care_slot.enums.AppointmentStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Patient contract has no internal slot, doctor ID, room ID or consultation fee. */
@Data @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientAppointmentResponse {
    private Long id;
    private String bookingCode;
    private Long patientProfileId;
    private String patientName;
    private Long clinicId;
    private String clinicName;
    private String clinicAddress;
    private Long specialtyId;
    private String specialtyName;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String symptomNote;
    private BigDecimal depositAmount;
    private AppointmentStatus status;
    private LocalDateTime holdExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime checkedInAt;
    private boolean assignmentVisible;
    private Long visitId;
    private String doctorName;
    private String roomName;
    private boolean paymentReviewRequired;
    private PaymentReceipt payment;

    @Data @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaymentReceipt {
        private String status;
        private BigDecimal amount;
        private String provider;
        private String transactionNo;
        private String txnRef;
        private String bankCode;
        private LocalDateTime paidAt;
    }
}
