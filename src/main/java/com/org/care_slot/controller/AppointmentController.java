package com.org.care_slot.controller;

import com.org.care_slot.dto.request.AppointmentCancelRequest;
import com.org.care_slot.dto.request.AppointmentCreateRequest;
import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.dto.response.AppointmentResponse;
import com.org.care_slot.dto.response.PageResponse;
import com.org.care_slot.enums.AppointmentStatus;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    private Long getEffectiveUserId(Long headerUserId) {
        if (headerUserId == null) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return headerUserId;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @Valid @RequestBody AppointmentCreateRequest request
    ) {
        Long userId = getEffectiveUserId(headerUserId);
        AppointmentResponse result = appointmentService.createAppointment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Appointment created successfully", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> getUserAppointments(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = getEffectiveUserId(headerUserId);
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<AppointmentResponse> result = appointmentService.getUserAppointments(userId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentDetail(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId
    ) {
        Long userId = getEffectiveUserId(headerUserId);
        AppointmentResponse result = appointmentService.getAppointmentDetail(id, userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/code/{bookingCode}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentByBookingCode(
            @PathVariable String bookingCode,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId
    ) {
        Long userId = getEffectiveUserId(headerUserId);
        AppointmentResponse result = appointmentService.getAppointmentByBookingCode(bookingCode, userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestBody(required = false) AppointmentCancelRequest request
    ) {
        Long userId = getEffectiveUserId(headerUserId);
        AppointmentResponse result = appointmentService.cancelAppointment(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled successfully", result));
    }
}
