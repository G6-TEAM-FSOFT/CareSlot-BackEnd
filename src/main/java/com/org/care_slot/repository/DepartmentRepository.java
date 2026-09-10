package com.org.care_slot.repository;

import com.org.care_slot.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByClinicId(Long clinicId);
    Optional<Department> findByClinicIdAndCode(Long clinicId, String code);
}
