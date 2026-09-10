package com.org.care_slot.repository;

import com.org.care_slot.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {
    List<ServiceRequest> findByClinicalOrderId(Long clinicalOrderId);
    List<ServiceRequest> findByVisitId(Long visitId);
}
