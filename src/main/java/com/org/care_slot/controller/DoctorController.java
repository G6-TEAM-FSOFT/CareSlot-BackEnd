package com.org.care_slot.controller;

import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.dto.response.DoctorDetailResponse;
import com.org.care_slot.dto.response.DoctorResponse;
import com.org.care_slot.dto.response.PageResponse;
import com.org.care_slot.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping("/api/v1/doctors")
    public ResponseEntity<ApiResponse<PageResponse<DoctorResponse>>> filterDoctors(
            @RequestParam(required = false) Long specialtyId,
            @RequestParam(required = false) Long clinicId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minFee,
            @RequestParam(required = false) BigDecimal maxFee,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<DoctorResponse> result = doctorService.filterDoctors(specialtyId, clinicId, keyword, minFee, maxFee, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/api/v1/doctors/{id}")
    public ResponseEntity<ApiResponse<DoctorDetailResponse>> getDoctorDetail(@PathVariable Long id) {
        DoctorDetailResponse result = doctorService.getDoctorDetail(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/api/v1/specialties/{specialtyId}/doctors")
    public ResponseEntity<ApiResponse<PageResponse<DoctorResponse>>> getDoctorsBySpecialty(
            @PathVariable Long specialtyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<DoctorResponse> result = doctorService.getDoctorsBySpecialty(specialtyId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
