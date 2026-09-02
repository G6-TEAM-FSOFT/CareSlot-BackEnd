package com.org.care_slot.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotBatchCreateRequest {

    @NotEmpty(message = "Slots list cannot be empty")
    private List<@Valid SlotCreateRequest> slots;
}
