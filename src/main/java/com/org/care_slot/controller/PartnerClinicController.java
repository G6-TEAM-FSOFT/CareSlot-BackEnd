package com.org.care_slot.controller;

import com.org.care_slot.dto.request.ClinicUpdateRequest;
import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.dto.response.ClinicDetailResponse;
import com.org.care_slot.service.ClinicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partner/clinic")
@RequiredArgsConstructor
public class PartnerClinicController {

    private final ClinicService clinicService;

    // Temporary fallback Clinic ID for testing/demo until SecurityContext JWT filter is fully wired
    private final Long MOCK_CLINIC_ID = 1L;

    private Long getEffectiveClinicId(Long headerClinicId) {
        return headerClinicId != null ? headerClinicId : MOCK_CLINIC_ID;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ClinicDetailResponse>> getMyClinic(
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        ClinicDetailResponse result = clinicService.getClinicDetail(clinicId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ClinicDetailResponse>> updateMyClinic(
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId,
            @Valid @RequestBody ClinicUpdateRequest request
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        ClinicDetailResponse result = clinicService.updateClinic(clinicId, request, clinicId);
        return ResponseEntity.ok(ApiResponse.success("Updated clinic information successfully", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClinicDetailResponse>> updateClinicById(
            @PathVariable Long id,
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId,
            @Valid @RequestBody ClinicUpdateRequest request
    ) {
        Long staffClinicId = getEffectiveClinicId(headerClinicId);
        ClinicDetailResponse result = clinicService.updateClinic(id, request, staffClinicId);
        return ResponseEntity.ok(ApiResponse.success("Updated clinic information successfully", result));
    }
}
