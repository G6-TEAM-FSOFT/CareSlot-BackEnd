package com.org.care_slot.repository;

import com.org.care_slot.entity.ResultTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResultTemplateRepository extends JpaRepository<ResultTemplate, Long> {
    Optional<ResultTemplate> findByCode(String code);
}
