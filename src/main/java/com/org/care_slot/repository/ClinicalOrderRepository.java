package com.org.care_slot.repository;

import com.org.care_slot.entity.ClinicalOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClinicalOrderRepository extends JpaRepository<ClinicalOrder, Long> {
    List<ClinicalOrder> findByVisitIdOrderByOrderRoundAsc(Long visitId);
    Optional<ClinicalOrder> findByOrderCode(String orderCode);
}
