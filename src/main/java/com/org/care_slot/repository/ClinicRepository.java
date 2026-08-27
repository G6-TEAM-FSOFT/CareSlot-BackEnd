package com.org.care_slot.repository;

import com.org.care_slot.entity.Clinic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {

    @Query("SELECT c FROM Clinic c WHERE " +
           "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND c.status = 'ACTIVE'")
    Page<Clinic> searchClinics(@Param("keyword") String keyword, Pageable pageable);
}
