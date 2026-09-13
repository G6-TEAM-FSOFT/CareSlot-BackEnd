package com.org.care_slot.dto.pdf;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record ExaminationPdfDto(
        Long visitId,
        Long patientId,
        String patientCode,
        String visitCode,
        String bookingCode,
        String patientName,
        LocalDate dateOfBirth,
        Integer age,
        String gender,
        String phone,
        String identityCard,
        String healthInsuranceCode,
        String ethnicity,
        String nationality,
        String occupation,
        String address,
        String clinicName,
        String clinicAddress,
        String departmentName,
        String specialtyName,
        String roomName,
        String doctorName,
        LocalDateTime visitDate,
        String appointmentDateText,
        String timeSlot,
        String statusText,
        BigDecimal consultationFee,
        String chiefComplaint,
        String symptomNote,
        String vitalSignsText,
        String diagnosisText,
        String treatmentPlan,
        String advice,
        String followUpDate
) {}
