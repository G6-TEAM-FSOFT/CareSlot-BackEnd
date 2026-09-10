package com.org.care_slot.dto.outpatient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionRequest {
    private Long visitId;
    private Long encounterId;
    private String diagnosisNote;
    private List<Item> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String drugName;
        private String dosage;
        private String usageInstruction;
        private Integer quantity;
        private String unit;
        private BigDecimal unitPrice;
    }
}
