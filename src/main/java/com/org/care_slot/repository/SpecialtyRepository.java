package com.org.care_slot.repository;

import com.org.care_slot.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    List<Specialty> findByClinics_IdAndStatus(Long clinicId, String status);

    @Query("SELECT DISTINCT s FROM Specialty s LEFT JOIN s.clinics c WHERE " +
           "(:clinicId IS NULL OR c.id = :clinicId) AND " +
           "(:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "s.status = 'ACTIVE'")
    List<Specialty> filterSpecialties(@Param("clinicId") Long clinicId, @Param("keyword") String keyword);

}
