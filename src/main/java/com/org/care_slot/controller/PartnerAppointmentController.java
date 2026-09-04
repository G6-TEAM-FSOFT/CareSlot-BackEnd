package com.org.care_slot.controller;

import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.dto.response.AppointmentResponse;
import com.org.care_slot.dto.response.BookingLogResponse;
import com.org.care_slot.dto.response.PageResponse;
import com.org.care_slot.enums.AppointmentStatus;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.service.AppointmentService;
import com.org.care_slot.service.BookingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/partner/appointments")
@RequiredArgsConstructor
public class PartnerAppointmentController {

    private final AppointmentService appointmentService;
    private final BookingLogService bookingLogService;

    private Long getEffectiveClinicId(Long headerClinicId) {
        if (headerClinicId == null) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }
        return headerClinicId;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> getClinicAppointments(
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<AppointmentResponse> result = appointmentService.getClinicAppointments(clinicId, status, doctorId, date, pageable, clinicId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getPartnerAppointmentDetail(
            @PathVariable Long id,
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        AppointmentResponse result = appointmentService.getPartnerAppointmentDetail(clinicId, id, clinicId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}/logs")
    public ResponseEntity<ApiResponse<List<BookingLogResponse>>> getAppointmentLogs(
            @PathVariable Long id,
            @RequestHeader(value = "X-Clinic-Id", required = false) Long headerClinicId
    ) {
        Long clinicId = getEffectiveClinicId(headerClinicId);
        // Verify ownership first
        appointmentService.getPartnerAppointmentDetail(clinicId, id, clinicId);
        List<BookingLogResponse> result = bookingLogService.getAppointmentLogs(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
