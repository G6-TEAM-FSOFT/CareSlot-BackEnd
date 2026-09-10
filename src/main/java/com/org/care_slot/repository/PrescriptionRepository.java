package com.org.care_slot.repository;

import com.org.care_slot.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Optional<Prescription> findByPrescriptionCode(String prescriptionCode);
    List<Prescription> findByVisitId(Long visitId);
    Optional<Prescription> findFirstByEncounterId(Long encounterId);
}
