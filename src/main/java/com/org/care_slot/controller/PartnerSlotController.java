package com.org.care_slot.controller;

import com.org.care_slot.dto.request.SlotBatchCreateRequest;
import com.org.care_slot.dto.request.SlotCreateRequest;
import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.dto.response.AppointmentSlotResponse;
import com.org.care_slot.dto.response.ExcelImportResultResponse;
import com.org.care_slot.enums.SlotStatus;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.service.AppointmentSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/partner/slots")
@RequiredArgsConstructor
public class PartnerSlotController {

    private final AppointmentSlotService appointmentSlotService;

    private Long getEffectiveClinicId(Long headerClinicId) {
        if (headerClinicId == null) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }
        return headerClinicId;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppointmentSlotResponse>>> getClinicSlots(
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) SlotStatus status
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        List<AppointmentSlotResponse> result = appointmentSlotService.getClinicSlots(clinicId, doctorId, date, fromDate, toDate, status, clinicId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentSlotResponse>> createSlot(
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId,
            @Valid @RequestBody SlotCreateRequest request
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        AppointmentSlotResponse result = appointmentSlotService.createSlot(clinicId, request, clinicId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created slot successfully", result));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<AppointmentSlotResponse>>> createBatchSlots(
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId,
            @Valid @RequestBody SlotBatchCreateRequest request
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        List<AppointmentSlotResponse> result = appointmentSlotService.createBatchSlots(clinicId, request, clinicId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created batch slots successfully", result));
    }

    @PostMapping("/import-excel")
    public ResponseEntity<ApiResponse<ExcelImportResultResponse>> importSlotsFromExcel(
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId,
            @RequestParam("file") MultipartFile file
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        ExcelImportResultResponse result = appointmentSlotService.importSlotsFromExcel(clinicId, file, clinicId);
        return ResponseEntity.ok(ApiResponse.success("Excel import processed", result));
    }
}
