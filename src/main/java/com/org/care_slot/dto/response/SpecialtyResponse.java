package com.org.care_slot.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialtyResponse {
    private Long id;
    private String name;
    private String description;
    private String status;
}
