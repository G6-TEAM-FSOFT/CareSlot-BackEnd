package com.org.care_slot.repository;

import com.org.care_slot.entity.AppointmentSlot;
import com.org.care_slot.enums.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentSlotRepository extends JpaRepository<AppointmentSlot, Long> {

       @Query("SELECT s FROM AppointmentSlot s JOIN FETCH s.doctor d JOIN FETCH d.clinic " +
              "JOIN FETCH d.specialty JOIN FETCH s.room r WHERE d.clinic.id = :clinicId " +
              "AND d.specialty.id = :specialtyId AND s.appointmentDate BETWEEN :fromDate AND :toDate " +
              "AND s.status = com.org.care_slot.enums.SlotStatus.AVAILABLE " +
              "AND d.status = 'ACTIVE' AND d.clinic.status = 'ACTIVE' AND d.specialty.status = 'ACTIVE' " +
              "AND r.status = 'ACTIVE' AND r.clinic.id = :clinicId AND r.roomType = 'CONSULTATION' " +
              "ORDER BY s.appointmentDate, s.startTime, s.endTime, d.id, s.id")
       List<AppointmentSlot> findBookingCandidates(@Param("clinicId") Long clinicId,
              @Param("specialtyId") Long specialtyId, @Param("fromDate") LocalDate fromDate,
              @Param("toDate") LocalDate toDate);

       @Query("SELECT s FROM AppointmentSlot s JOIN FETCH s.doctor LEFT JOIN FETCH s.room " +
              "WHERE s.doctor.clinic.id = :clinicId AND s.appointmentDate BETWEEN :fromDate AND :toDate " +
              "AND s.status IN (com.org.care_slot.enums.SlotStatus.HELD, com.org.care_slot.enums.SlotStatus.BOOKED)")
       List<AppointmentSlot> findOccupiedSlots(@Param("clinicId") Long clinicId,
              @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

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

       @Query("SELECT s FROM AppointmentSlot s WHERE " +
                     "s.doctor.clinic.id = :clinicId AND " +
                     "(:doctorId IS NULL OR s.doctor.id = :doctorId) AND " +
                     "(:date IS NULL OR s.appointmentDate = :date) AND " +
                     "(:fromDate IS NULL OR s.appointmentDate >= :fromDate) AND " +
                     "(:toDate IS NULL OR s.appointmentDate <= :toDate) AND " +
                     "(:status IS NULL OR s.status = :status) " +
                     "ORDER BY s.appointmentDate ASC, s.startTime ASC")
       List<AppointmentSlot> findClinicSlots(@Param("clinicId") Long clinicId,
                     @Param("doctorId") Long doctorId,
                     @Param("date") LocalDate date,
                     @Param("fromDate") LocalDate fromDate,
                     @Param("toDate") LocalDate toDate,
                     @Param("status") SlotStatus status);

       @Query("SELECT COUNT(s) > 0 FROM AppointmentSlot s WHERE s.doctor.id = :doctorId " +
                     "AND s.appointmentDate = :date " +
                     "AND s.startTime < :endTime AND s.endTime > :startTime")
       boolean existsOverlappingSlot(@Param("doctorId") Long doctorId,
                     @Param("date") LocalDate date,
                     @Param("startTime") LocalTime startTime,
                     @Param("endTime") LocalTime endTime);

       List<AppointmentSlot> findByStatusAndHoldExpiresAtBefore(SlotStatus status, java.time.LocalDateTime dateTime);

       @Query("SELECT s FROM AppointmentSlot s WHERE " +
                     "s.doctor.clinic.id IN :clinicIds AND " +
                     "s.status = com.org.care_slot.enums.SlotStatus.AVAILABLE AND " +
                     "(s.appointmentDate > :today OR (s.appointmentDate = :today AND s.startTime > :nowTime)) " +
                     "ORDER BY s.appointmentDate ASC, s.startTime ASC")
       List<AppointmentSlot> findFutureAvailableSlotsByClinicIds(
                     @Param("clinicIds") List<Long> clinicIds,
                     @Param("today") LocalDate today,
                     @Param("nowTime") LocalTime nowTime);

       @Query("SELECT s FROM AppointmentSlot s WHERE s.status = :status AND " +
              "(s.appointmentDate < :currentDate OR (s.appointmentDate = :currentDate AND s.startTime <= :currentTime))")
       List<AppointmentSlot> findOverdueSlots(@Param("status") SlotStatus status,
                                              @Param("currentDate") LocalDate currentDate,
                                              @Param("currentTime") LocalTime currentTime);

       @Modifying(clearAutomatically = true)
       @Query("UPDATE AppointmentSlot s SET s.status = :heldStatus, " +
              "s.heldAt = :heldAt, s.holdExpiresAt = :holdExpiresAt " +
              "WHERE s.id = :slotId AND s.status = :availableStatus")
       int holdSlotAtomic(@Param("slotId") Long slotId,
                          @Param("heldAt") LocalDateTime heldAt,
                          @Param("holdExpiresAt") LocalDateTime holdExpiresAt,
                          @Param("heldStatus") SlotStatus heldStatus,
                          @Param("availableStatus") SlotStatus availableStatus);

       default int holdSlotAtomic(Long slotId, LocalDateTime heldAt, LocalDateTime holdExpiresAt) {
              return holdSlotAtomic(slotId, heldAt, holdExpiresAt, SlotStatus.HELD, SlotStatus.AVAILABLE);
       }
}
