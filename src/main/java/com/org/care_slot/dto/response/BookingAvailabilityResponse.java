package com.org.care_slot.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

/** Public availability intentionally contains neither internal resource IDs nor capacity. */
public record BookingAvailabilityResponse(LocalDate appointmentDate, LocalTime startTime, LocalTime endTime) {}
