package com.org.care_slot.controller;

import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.dto.response.BookingAvailabilityResponse;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.service.SlotAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/v1/booking")
@RequiredArgsConstructor
public class BookingAvailabilityController {
    private final SlotAllocationService allocationService;

    @GetMapping("/availability")
    public ApiResponse<List<BookingAvailabilityResponse>> availability(
            @RequestParam Long clinicId, @RequestParam Long specialtyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        if (clinicId <= 0 || specialtyId <= 0 || toDate.isBefore(fromDate)
                || ChronoUnit.DAYS.between(fromDate, toDate) > 30) {
            throw new AppException(ErrorCode.INVALID_SLOT_TIME);
        }
        return ApiResponse.success(allocationService.findAvailableSlots(clinicId, specialtyId, fromDate, toDate, null)
                .stream().map(s -> new BookingAvailabilityResponse(s.getAppointmentDate(), s.getStartTime(), s.getEndTime()))
                .distinct().toList());
    }
}
