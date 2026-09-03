package com.org.care_slot.service.impl;

import com.org.care_slot.dto.response.BookingLogResponse;
import com.org.care_slot.entity.Appointment;
import com.org.care_slot.entity.BookingLog;
import com.org.care_slot.repository.BookingLogRepository;
import com.org.care_slot.service.BookingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingLogServiceImpl implements BookingLogService {

    private final BookingLogRepository bookingLogRepository;

    @Override
    @Transactional
    public void logEvent(Appointment appointment, String previousStatus, String newStatus, String eventType, String note, String actor) {
        if (appointment == null) return;

        BookingLog log = BookingLog.builder()
                .appointment(appointment)
                .bookingCode(appointment.getBookingCode())
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .eventType(eventType != null ? eventType : newStatus)
                .note(note)
                .actor(actor != null ? actor : "SYSTEM")
                .build();

        bookingLogRepository.save(log);
    }

    @Override
    public List<BookingLogResponse> getAppointmentLogs(Long appointmentId) {
        return bookingLogRepository.findByAppointmentIdOrderByCreatedAtAsc(appointmentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    private BookingLogResponse mapToResponse(BookingLog log) {
        return BookingLogResponse.builder()
                .id(log.getId())
                .appointmentId(log.getAppointment() != null ? log.getAppointment().getId() : null)
                .bookingCode(log.getBookingCode())
                .previousStatus(log.getPreviousStatus())
                .newStatus(log.getNewStatus())
                .eventType(log.getEventType())
                .note(log.getNote())
                .actor(log.getActor())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
