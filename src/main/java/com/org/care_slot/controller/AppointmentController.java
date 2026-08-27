package com.org.care_slot.controller;

import com.org.care_slot.dto.request.AppointmentCancelRequest;
import com.org.care_slot.dto.request.AppointmentCreateRequest;
import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.dto.response.AppointmentResponse;
import com.org.care_slot.dto.response.PageResponse;
import com.org.care_slot.enums.AppointmentStatus;
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

    // Temporary fallback User ID for testing/demo until SecurityContext is fully wired
    private final Long MOCK_USER_ID = 3L;

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody AppointmentCreateRequest request
    ) {
        AppointmentResponse result = appointmentService.createAppointment(MOCK_USER_ID, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Appointment created successfully", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> getUserAppointments(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageResponse<AppointmentResponse> result = appointmentService.getUserAppointments(MOCK_USER_ID, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentDetail(@PathVariable Long id) {
        AppointmentResponse result = appointmentService.getAppointmentDetail(id, MOCK_USER_ID);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/code/{bookingCode}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentByBookingCode(@PathVariable String bookingCode) {
        AppointmentResponse result = appointmentService.getAppointmentByBookingCode(bookingCode, MOCK_USER_ID);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) AppointmentCancelRequest request
    ) {
        AppointmentResponse result = appointmentService.cancelAppointment(id, MOCK_USER_ID, request);
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled successfully", result));
    }
}
