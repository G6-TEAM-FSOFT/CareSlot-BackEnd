package com.org.care_slot.repository;

import com.org.care_slot.entity.Appointment;
import com.org.care_slot.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    @Query("SELECT a FROM Appointment a WHERE " +
            "a.slot.doctor.clinic.id = :clinicId AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:doctorId IS NULL OR a.slot.doctor.id = :doctorId) AND " +
            "(:date IS NULL OR a.slot.appointmentDate = :date) " +
            "ORDER BY a.createdAt DESC")
    Page<Appointment> findClinicAppointments(@Param("clinicId") Long clinicId,
            @Param("status") AppointmentStatus status,
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            Pageable pageable);

    Optional<Appointment> findBySlotIdAndStatus(Long slotId, AppointmentStatus status);
}
