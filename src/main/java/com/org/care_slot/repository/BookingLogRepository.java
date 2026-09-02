package com.org.care_slot.repository;

import com.org.care_slot.entity.BookingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingLogRepository extends JpaRepository<BookingLog, Long> {
    List<BookingLog> findByAppointmentIdOrderByCreatedAtAsc(Long appointmentId);
    List<BookingLog> findByBookingCodeOrderByCreatedAtAsc(String bookingCode);
}
