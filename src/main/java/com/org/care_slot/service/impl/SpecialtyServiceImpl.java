package com.org.care_slot.service.impl;

import com.org.care_slot.dto.response.SpecialtyResponse;
import com.org.care_slot.entity.Specialty;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.repository.SpecialtyRepository;
import com.org.care_slot.service.SpecialtyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpecialtyServiceImpl implements SpecialtyService {

    private final SpecialtyRepository specialtyRepository;

    @Override
    public List<SpecialtyResponse> filterSpecialties(Long clinicId, String keyword) {
        return specialtyRepository.filterSpecialties(clinicId, keyword).stream()
                .map(this::mapToSpecialtyResponse)
                .toList();
    }

    @Override
    public SpecialtyResponse getSpecialtyDetail(Long id) {
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SPECIALTY_NOT_FOUND));
        return mapToSpecialtyResponse(specialty);
    }

    @Override
    public List<SpecialtyResponse> getSpecialtiesByClinic(Long clinicId) {
        return specialtyRepository.findByClinics_IdAndStatus(clinicId, "ACTIVE").stream()
                .map(this::mapToSpecialtyResponse)
                .toList();
    }

    private SpecialtyResponse mapToSpecialtyResponse(Specialty specialty) {
        return SpecialtyResponse.builder()
                .id(specialty.getId())
                .name(specialty.getName())
                .description(specialty.getDescription())
                .status(specialty.getStatus())
                .build();
    }
}
