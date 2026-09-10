package com.org.care_slot.repository;

import com.org.care_slot.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VisitRepository extends JpaRepository<Visit, Long> {
    Optional<Visit> findByVisitCode(String visitCode);
    Optional<Visit> findByAppointmentId(Long appointmentId);
    List<Visit> findByPatientProfileIdOrderByCreatedAtDesc(Long patientProfileId);
    List<Visit> findByClinicIdAndStatus(Long clinicId, String status);
}
