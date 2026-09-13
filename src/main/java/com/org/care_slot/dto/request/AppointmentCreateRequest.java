package com.org.care_slot.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCreateRequest {

    @NotNull(message = "Patient profile ID is required")
    @Positive
    private Long patientProfileId;

    @NotNull
    @Positive
    private Long clinicId;

    @NotNull
    @Positive
    private Long specialtyId;

    @NotNull
    private LocalDate appointmentDate;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @Size(max = 2000)
    private String symptomNote;

    @NotBlank
    @Size(max = 80)
    private String requestKey;
}
