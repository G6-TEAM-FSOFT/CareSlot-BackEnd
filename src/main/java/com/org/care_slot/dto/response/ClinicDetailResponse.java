package com.org.care_slot.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicDetailResponse {
    private Long id;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String phone;
    private String description;
    private String status;
    private List<SpecialtyResponse> specialties;
}
