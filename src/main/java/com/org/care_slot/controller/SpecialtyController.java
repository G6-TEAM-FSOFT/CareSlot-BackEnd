package com.org.care_slot.controller;

import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.dto.response.SpecialtyResponse;
import com.org.care_slot.service.SpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    @GetMapping("/api/v1/specialties")
    public ResponseEntity<ApiResponse<List<SpecialtyResponse>>> filterSpecialties(
            @RequestParam(required = false) Long clinicId,
            @RequestParam(required = false) String keyword
    ) {
        List<SpecialtyResponse> result = specialtyService.filterSpecialties(clinicId, keyword);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/api/v1/specialties/{id}")
    public ResponseEntity<ApiResponse<SpecialtyResponse>> getSpecialtyDetail(@PathVariable Long id) {
        SpecialtyResponse result = specialtyService.getSpecialtyDetail(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/api/v1/clinics/{clinicId}/specialties")
    public ResponseEntity<ApiResponse<List<SpecialtyResponse>>> getSpecialtiesByClinic(@PathVariable Long clinicId) {
        List<SpecialtyResponse> result = specialtyService.getSpecialtiesByClinic(clinicId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
