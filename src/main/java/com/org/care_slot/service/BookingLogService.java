package com.org.care_slot.service;

import com.org.care_slot.dto.response.BookingLogResponse;
import com.org.care_slot.entity.Appointment;

import java.util.List;

public interface BookingLogService {
    void logEvent(Appointment appointment, String previousStatus, String newStatus, String eventType, String note, String actor);
    List<BookingLogResponse> getAppointmentLogs(Long appointmentId);
}
