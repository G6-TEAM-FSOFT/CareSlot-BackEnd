package com.org.care_slot.dto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelImportResultResponse {

    private int totalRows;
    private int successCount;
    private int failedCount;

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    @Builder.Default
    private List<AppointmentSlotResponse> importedSlots = new ArrayList<>();
}
