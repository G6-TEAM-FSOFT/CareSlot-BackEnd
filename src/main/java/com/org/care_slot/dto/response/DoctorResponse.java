package com.org.care_slot.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {
    private Long id;
    private String fullName;
    private String title;
    private String avatarUrl;
    private BigDecimal consultationFee;
    private Long clinicId;
    private String clinicName;
    private Long specialtyId;
    private String specialtyName;
    private String status;
}
