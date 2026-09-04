package com.org.care_slot.dto.response;

import com.org.care_slot.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private RoleType role;
    private Long clinicId;
    private String clinicName;
    private String token;
}
