package com.org.care_slot.dto.outpatient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VitalSignRequest {
    private Long visitId;
    private Long encounterId;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private BigDecimal temperatureC;
    private Integer heartRateBpm;
    private Integer respiratoryRate;
    private Integer systolicBp;
    private Integer diastolicBp;
    private BigDecimal spo2;
}
