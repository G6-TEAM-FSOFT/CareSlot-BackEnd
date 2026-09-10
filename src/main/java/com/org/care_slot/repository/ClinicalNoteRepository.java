package com.org.care_slot.repository;

import com.org.care_slot.entity.ClinicalNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicalNoteRepository extends JpaRepository<ClinicalNote, Long> {
    List<ClinicalNote> findByVisitId(Long visitId);
    Optional<ClinicalNote> findFirstByEncounterId(Long encounterId);
}
