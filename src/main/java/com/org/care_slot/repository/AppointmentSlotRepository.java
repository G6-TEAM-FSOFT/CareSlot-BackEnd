package com.org.care_slot.repository;

import com.org.care_slot.entity.AppointmentSlot;
import com.org.care_slot.enums.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {

    @Query("SELECT s FROM AppointmentSlot s WHERE " +
           "s.doctor.id = :doctorId AND " +
           "(:date IS NULL OR s.appointmentDate = :date) AND " +
           "(:fromDate IS NULL OR s.appointmentDate >= :fromDate) AND " +
           "(:toDate IS NULL OR s.appointmentDate <= :toDate) AND " +
           "(:status IS NULL OR s.status = :status) " +
           "ORDER BY s.appointmentDate ASC, s.startTime ASC")
    List<AppointmentSlot> findDoctorSlots(@Param("doctorId") Long doctorId,
                                          @Param("date") LocalDate date,
                                          @Param("fromDate") LocalDate fromDate,
                                          @Param("toDate") LocalDate toDate,
                                          @Param("status") SlotStatus status);
}
