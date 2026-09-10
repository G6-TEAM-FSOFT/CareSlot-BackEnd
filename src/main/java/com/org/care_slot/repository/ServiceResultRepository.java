package com.org.care_slot.repository;

import com.org.care_slot.entity.ServiceResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceResultRepository extends JpaRepository<ServiceResult, Long> {
    Optional<ServiceResult> findByServiceRequestId(Long serviceRequestId);
}
