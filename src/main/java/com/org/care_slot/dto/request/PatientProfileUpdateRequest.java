package com.org.care_slot.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientProfileUpdateRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private String identityCard;
    private LocalDate cardIssueDate;
    private String ethnicity;
    private String nationality;
    private String occupation;
    private String address;
    private String relationship;
}
