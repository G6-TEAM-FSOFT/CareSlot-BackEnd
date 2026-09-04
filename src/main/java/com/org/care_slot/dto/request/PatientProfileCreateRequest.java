package com.org.care_slot.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientProfileCreateRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private String identityCard;
    private LocalDate cardIssueDate;

    @Builder.Default
    private String ethnicity = "Kinh";

    @Builder.Default
    private String nationality = "Việt Nam";

    private String occupation;
    private String address;

    @Builder.Default
    private String relationship = "SELF";
}
