package com.org.care_slot.controller;

import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.dto.response.AppointmentSlotResponse;
import com.org.care_slot.enums.SlotStatus;
import com.org.care_slot.service.AppointmentSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors/{doctorId}/slots")
@RequiredArgsConstructor
public class AppointmentSlotController {

    private final AppointmentSlotService appointmentSlotService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppointmentSlotResponse>>> getDoctorSlots(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) SlotStatus status
    ) {
        List<AppointmentSlotResponse> result = appointmentSlotService.getDoctorSlots(doctorId, date, fromDate, toDate, status);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
