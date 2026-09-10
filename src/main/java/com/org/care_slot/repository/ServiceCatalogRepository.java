package com.org.care_slot.repository;

import com.org.care_slot.entity.ServiceCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCatalogRepository extends JpaRepository<ServiceCatalog, Long> {
    List<ServiceCatalog> findByClinicId(Long clinicId);
    Optional<ServiceCatalog> findByClinicIdAndCode(Long clinicId, String code);
}
