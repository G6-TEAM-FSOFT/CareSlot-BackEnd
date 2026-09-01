package com.org.care_slot.repository;

import com.org.care_slot.entity.PatientProfile;
import com.org.care_slot.enums.ProfileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {

    @Query("SELECT p FROM PatientProfile p WHERE " +
           "p.user.id = :userId AND " +
           "(:keyword IS NULL OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "p.status = 'ACTIVE'")
    List<PatientProfile> findByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    Optional<PatientProfile> findByIdAndUserIdAndStatus(Long id, Long userId, String status);

    Optional<PatientProfile> findByUserIdAndProfileTypeAndStatus(Long userId, ProfileType profileType, String status);

    Optional<PatientProfile> findByUserIdAndProfileType(Long userId, ProfileType profileType);
}
