package com.org.care_slot.controller;

import com.org.care_slot.dto.request.DoctorCreateRequest;
import com.org.care_slot.dto.request.DoctorUpdateRequest;
import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.dto.response.DoctorDetailResponse;
import com.org.care_slot.dto.response.DoctorResponse;
import com.org.care_slot.dto.response.PageResponse;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.service.DoctorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/partner/doctors")
@RequiredArgsConstructor
public class PartnerDoctorController {

    private final DoctorService doctorService;

    private Long getEffectiveClinicId(Long headerClinicId) {
        if (headerClinicId == null) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }
        return headerClinicId;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DoctorResponse>>> getPartnerDoctors(
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId,
            @RequestParam(required = false) Long specialtyId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<DoctorResponse> result = doctorService.getPartnerDoctors(clinicId, specialtyId, keyword, status, pageable, clinicId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorDetailResponse>> getPartnerDoctorDetail(
            @PathVariable Long id,
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        DoctorDetailResponse result = doctorService.getPartnerDoctorDetail(clinicId, id, clinicId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DoctorDetailResponse>> createDoctor(
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId,
            @Valid @RequestBody DoctorCreateRequest request
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        DoctorDetailResponse result = doctorService.createDoctor(clinicId, request, clinicId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created doctor successfully", result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorDetailResponse>> updateDoctor(
            @PathVariable Long id,
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId,
            @Valid @RequestBody DoctorUpdateRequest request
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        DoctorDetailResponse result = doctorService.updateDoctor(clinicId, id, request, clinicId);
        return ResponseEntity.ok(ApiResponse.success("Updated doctor successfully", result));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DoctorDetailResponse>> updateDoctorStatus(
            @PathVariable Long id,
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId,
            @RequestParam String status
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        DoctorDetailResponse result = doctorService.updateDoctorStatus(clinicId, id, status, clinicId);
        return ResponseEntity.ok(ApiResponse.success("Updated doctor status successfully", result));
    }
}
