package com.org.care_slot.repository;

import com.org.care_slot.entity.MedicalRecordTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordTemplateRepository extends JpaRepository<MedicalRecordTemplate, Long> {
    List<MedicalRecordTemplate> findBySpecialtyId(Long specialtyId);
    Optional<MedicalRecordTemplate> findFirstBySpecialtyIdAndStatusOrderByVersionDesc(Long specialtyId, String status);
}
