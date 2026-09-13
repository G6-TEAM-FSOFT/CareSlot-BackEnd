package com.org.care_slot.repository;

import com.org.care_slot.entity.Encounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EncounterRepository extends JpaRepository<Encounter, Long> {
    List<Encounter> findByVisitIdOrderByCreatedAtAsc(Long visitId);
    List<Encounter> findByRoomIdOrderByCreatedAtAsc(Long roomId);
    List<Encounter> findByRoomIdAndStatusOrderByCreatedAtAsc(Long roomId, String status);
    List<Encounter> findByDoctorIdAndStatusOrderByCreatedAtAsc(Long doctorId, String status);
    List<Encounter> findByRoomIdAndDoctorIdAndStatusOrderByCreatedAtAsc(Long roomId, Long doctorId, String status);
    List<Encounter> findByRoomIdAndDoctorIdOrderByCreatedAtAsc(Long roomId, Long doctorId);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT e.room FROM Encounter e WHERE e.doctor.id = :doctorId AND e.room IS NOT NULL")
    List<com.org.care_slot.entity.Room> findRoomsByDoctorId(@org.springframework.data.repository.query.Param("doctorId") Long doctorId);
}
