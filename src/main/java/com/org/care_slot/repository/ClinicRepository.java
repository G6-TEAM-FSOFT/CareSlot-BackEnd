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

    @Query(value = "SELECT DISTINCT c FROM Clinic c " +
           "LEFT JOIN c.specialties s " +
           "WHERE c.status = 'ACTIVE' " +
           "AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:specialtyId IS NULL OR (s.id = :specialtyId AND s.status = 'ACTIVE')) " +
           "AND (:location IS NULL OR LOWER(c.address) LIKE LOWER(CONCAT('%', :location, '%')))",
           countQuery = "SELECT COUNT(DISTINCT c) FROM Clinic c " +
           "LEFT JOIN c.specialties s " +
           "WHERE c.status = 'ACTIVE' " +
           "AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(c.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:specialtyId IS NULL OR (s.id = :specialtyId AND s.status = 'ACTIVE')) " +
           "AND (:location IS NULL OR LOWER(c.address) LIKE LOWER(CONCAT('%', :location, '%')))")
    Page<Clinic> searchClinics(
            @Param("keyword") String keyword,
            @Param("specialtyId") Long specialtyId,
            @Param("location") String location,
            Pageable pageable
    );
}
