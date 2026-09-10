package com.org.care_slot.dto.outpatient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitDispositionRequest {
    private Long visitId;
    private String dispositionType; // OUTPATIENT, REFERRED, ADMITTED
    private String notes;
    private String destinationFacility;
    private String destinationDepartment;
}
