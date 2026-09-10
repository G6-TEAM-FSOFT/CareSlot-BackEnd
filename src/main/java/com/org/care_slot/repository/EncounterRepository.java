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
}
