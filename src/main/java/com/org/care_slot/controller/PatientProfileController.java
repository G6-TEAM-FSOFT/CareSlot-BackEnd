package com.org.care_slot.controller;

import com.org.care_slot.dto.request.PatientProfileCreateRequest;
import com.org.care_slot.dto.request.PatientProfileUpdateRequest;
import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.dto.response.PatientProfileResponse;
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

    // Temporary fallback User ID for testing/demo until SecurityContext is fully
    // wired
    private final Long MOCK_USER_ID = 3L;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientProfileResponse>>> getPatientProfiles(
            @RequestParam(required = false) String keyword) {
        List<PatientProfileResponse> result = patientProfileService.getPatientProfiles(MOCK_USER_ID, keyword);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> getPatientProfileDetail(@PathVariable Long id) {
        PatientProfileResponse result = patientProfileService.getPatientProfileDetail(id, MOCK_USER_ID);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PatientProfileResponse>> createPatientProfile(
            @Valid @RequestBody PatientProfileCreateRequest request) {
        PatientProfileResponse result = patientProfileService.createPatientProfile(MOCK_USER_ID, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Created patient profile successfully", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> updatePatientProfile(
            @PathVariable Long id,
            @Valid @RequestBody PatientProfileUpdateRequest request) {
        PatientProfileResponse result = patientProfileService.updatePatientProfile(id, MOCK_USER_ID, request);
        return ResponseEntity.ok(ApiResponse.success("Updated patient profile successfully", result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePatientProfile(@PathVariable Long id) {
        patientProfileService.deletePatientProfile(id, MOCK_USER_ID);
        return ResponseEntity.ok(ApiResponse.success("Deleted patient profile successfully", null));
    }
}
