package com.org.care_slot.controller;

import com.org.care_slot.dto.outpatient.VisitDetailResponse;
import com.org.care_slot.dto.request.ClinicUpdateRequest;
import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.dto.response.ClinicDetailResponse;
import com.org.care_slot.dto.response.SpecialtyResponse;
import com.org.care_slot.entity.User;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.service.ClinicService;
import com.org.care_slot.service.OutpatientWorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.org.care_slot.security.CurrentUserProvider;

@RestController
@RequestMapping("/api/v1/partner/clinic")
@RequiredArgsConstructor
public class PartnerClinicController {

    private final ClinicService clinicService;
    private final OutpatientWorkflowService outpatientWorkflowService;
    private final CurrentUserProvider currentUserProvider;

    private Long getEffectiveClinicId(Long headerClinicId) {
        if (headerClinicId != null) {
            return headerClinicId;
        }
        try {
            return currentUserProvider.getCurrentClinicId();
        } catch (Exception e) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }
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

    @GetMapping("/specialties")
    public ResponseEntity<ApiResponse<List<SpecialtyResponse>>> getMyClinicSpecialties(
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        List<SpecialtyResponse> result = clinicService.getClinicSpecialties(clinicId, clinicId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/specialties/{specialtyId}")
    public ResponseEntity<ApiResponse<ClinicDetailResponse>> addSpecialtyToMyClinic(
            @PathVariable Long specialtyId,
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        ClinicDetailResponse result = clinicService.addSpecialtyToClinic(clinicId, specialtyId, clinicId);
        return ResponseEntity.ok(ApiResponse.success("Added specialty to clinic successfully", result));
    }

    @DeleteMapping("/specialties/{specialtyId}")
    public ResponseEntity<ApiResponse<ClinicDetailResponse>> removeSpecialtyFromMyClinic(
            @PathVariable Long specialtyId,
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        ClinicDetailResponse result = clinicService.removeSpecialtyFromClinic(clinicId, specialtyId, clinicId);
        return ResponseEntity.ok(ApiResponse.success("Removed specialty from clinic successfully", result));
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<VisitDetailResponse.RoomDto>>> getMyClinicRooms(
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        Long currentUserId = null;
        try {
            User user = currentUserProvider.getCurrentUser();
            if (user != null) {
                currentUserId = user.getId();
            }
        } catch (Exception ignored) {}
        List<VisitDetailResponse.RoomDto> result = outpatientWorkflowService.getRooms(clinicId, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
