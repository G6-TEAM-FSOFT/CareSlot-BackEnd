package com.org.care_slot.dto.response;

import com.org.care_slot.enums.ProfileType;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientProfileResponse {
    private Long id;
    private Long userId;
    private ProfileType profileType;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private String relationship;
    private String status;
}
