package com.org.care_slot.controller;

import com.org.care_slot.dto.request.PatientProfileCreateRequest;
import com.org.care_slot.dto.request.PatientProfileUpdateRequest;
import com.org.care_slot.dto.request.UpdatePrimaryProfileRequest;
import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.dto.response.PatientProfileResponse;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.security.CurrentUserProvider;
import com.org.care_slot.service.PatientProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientProfileController {

    private final PatientProfileService patientProfileService;
    private final CurrentUserProvider currentUserProvider;

    private Long getEffectiveUserId(Long headerUserId) {
        if (headerUserId != null) {
            return headerUserId;
        }
        try {
            return currentUserProvider.getCurrentPatientUserId();
        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    @GetMapping("/me/primary")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> getMyPrimaryProfile(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId
    ) {
        Long userId = getEffectiveUserId(headerUserId);
        PatientProfileResponse result = patientProfileService.getMyPrimaryProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/me/primary")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> updateMyPrimaryProfile(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @Valid @RequestBody UpdatePrimaryProfileRequest request
    ) {
        Long userId = getEffectiveUserId(headerUserId);
        PatientProfileResponse result = patientProfileService.updateMyPrimaryProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Updated patient profile successfully", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientProfileResponse>>> getPatientProfiles(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(required = false) String keyword
    ) {
        Long userId = getEffectiveUserId(headerUserId);
        List<PatientProfileResponse> result = patientProfileService.getPatientProfiles(userId, keyword);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> getPatientProfileDetail(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId
    ) {
        Long userId = getEffectiveUserId(headerUserId);
        PatientProfileResponse result = patientProfileService.getPatientProfileDetail(id, userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PatientProfileResponse>> createPatientProfile(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @Valid @RequestBody PatientProfileCreateRequest request
    ) {
        Long userId = getEffectiveUserId(headerUserId);
        PatientProfileResponse result = patientProfileService.createPatientProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created patient profile successfully", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> updatePatientProfile(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @Valid @RequestBody PatientProfileUpdateRequest request
    ) {
        Long userId = getEffectiveUserId(headerUserId);
        PatientProfileResponse result = patientProfileService.updatePatientProfile(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Updated patient profile successfully", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePatientProfile(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId
    ) {
        Long userId = getEffectiveUserId(headerUserId);
        patientProfileService.deletePatientProfile(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Deleted patient profile successfully", null));
    }
}
