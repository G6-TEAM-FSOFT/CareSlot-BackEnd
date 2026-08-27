package com.org.care_slot.repository;

import com.org.care_slot.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @Query("SELECT d FROM Doctor d WHERE " +
           "(:specialtyId IS NULL OR d.specialty.id = :specialtyId) AND " +
           "(:clinicId IS NULL OR d.clinic.id = :clinicId) AND " +
           "(:keyword IS NULL OR LOWER(d.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:minFee IS NULL OR d.consultationFee >= :minFee) AND " +
           "(:maxFee IS NULL OR d.consultationFee <= :maxFee) AND " +
           "d.status = 'ACTIVE'")
    Page<Doctor> filterDoctors(@Param("specialtyId") Long specialtyId,
                               @Param("clinicId") Long clinicId,
                               @Param("keyword") String keyword,
                               @Param("minFee") BigDecimal minFee,
                               @Param("maxFee") BigDecimal maxFee,
                               Pageable pageable);

    Page<Doctor> findBySpecialtyIdAndStatus(Long specialtyId, String status, Pageable pageable);
}
