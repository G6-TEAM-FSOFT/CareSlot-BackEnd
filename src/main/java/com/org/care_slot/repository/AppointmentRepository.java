package com.org.care_slot.repository;

import com.org.care_slot.entity.Appointment;
import com.org.care_slot.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByBookingCode(String bookingCode);

    @Query("SELECT a FROM Appointment a WHERE " +
           "a.patientProfile.user.id = :userId AND " +
           "(:status IS NULL OR a.status = :status)")
    Page<Appointment> findByUserIdAndStatus(@Param("userId") Long userId,
                                            @Param("status") AppointmentStatus status,
                                            Pageable pageable);
}
