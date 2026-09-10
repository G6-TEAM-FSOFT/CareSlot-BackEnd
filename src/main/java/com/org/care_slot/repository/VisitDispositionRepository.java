package com.org.care_slot.repository;

import com.org.care_slot.entity.VisitDisposition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VisitDispositionRepository extends JpaRepository<VisitDisposition, Long> {
    Optional<VisitDisposition> findByVisitId(Long visitId);
}
