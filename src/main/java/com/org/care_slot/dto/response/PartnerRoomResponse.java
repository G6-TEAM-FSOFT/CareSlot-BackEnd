package com.org.care_slot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartnerRoomResponse {

    private Long id;
    private Long clinicId;
    private Long departmentId;
    private String departmentName;
    private String roomNumber;
    private String name;
    private String roomType;
    private String status;

    private long totalSlots;
    private long availableSlots;
    private long bookedSlots;
    private long heldSlots;
}
