package com.org.care_slot.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomUpdateRequest {

    private Long departmentId;
    private String roomNumber;
    private String name;
    private String roomType;
    private String status;
}
