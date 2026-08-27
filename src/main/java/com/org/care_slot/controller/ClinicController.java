package com.org.care_slot.controller;

import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.dto.response.ClinicDetailResponse;
import com.org.care_slot.dto.response.ClinicResponse;
import com.org.care_slot.dto.response.PageResponse;
import com.org.care_slot.service.ClinicService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/clinics")
@RequiredArgsConstructor
public class ClinicController {

    private final ClinicService clinicService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ClinicResponse>>> getClinics(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<ClinicResponse> result = clinicService.getClinics(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClinicDetailResponse>> getClinicDetail(@PathVariable Long id) {
        ClinicDetailResponse result = clinicService.getClinicDetail(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
