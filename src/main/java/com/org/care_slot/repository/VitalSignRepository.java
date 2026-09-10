package com.org.care_slot.repository;

import com.org.care_slot.entity.VitalSign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VitalSignRepository extends JpaRepository<VitalSign, Long> {
    List<VitalSign> findByVisitId(Long visitId);
    Optional<VitalSign> findFirstByEncounterId(Long encounterId);
}
