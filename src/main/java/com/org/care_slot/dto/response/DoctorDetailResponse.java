package com.org.care_slot.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDetailResponse {
    private Long id;
    private String fullName;
    private String title;
    private String bio;
    private String avatarUrl;
    private BigDecimal consultationFee;
    private String status;
    private ClinicResponse clinic;
    private SpecialtyResponse specialty;
}
