package com.org.care_slot.repository;

import com.org.care_slot.entity.MedicalConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalConsultationRepository extends JpaRepository<MedicalConsultation, Long> {
    Optional<MedicalConsultation> findByIdAndPatientIdAndVisitId(Long id, Long patientId, Long visitId);
    List<MedicalConsultation> findByPatientIdAndVisitId(Long patientId, Long visitId);
}
