package com.org.care_slot.dto.outpatient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicalOrderRequest {
    private Long visitId;
    private Long encounterId;
    private List<Long> serviceIds;
}
