package com.org.care_slot.service.impl;

import com.org.care_slot.dto.response.ClinicDetailResponse;
import com.org.care_slot.dto.response.ClinicResponse;
import com.org.care_slot.dto.response.PageResponse;
import com.org.care_slot.dto.response.SpecialtyResponse;
import com.org.care_slot.entity.Clinic;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.repository.ClinicRepository;
import com.org.care_slot.repository.SpecialtyRepository;
import com.org.care_slot.service.ClinicService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClinicServiceImpl implements ClinicService {

    private final ClinicRepository clinicRepository;
    private final SpecialtyRepository specialtyRepository;

    @Override
    public PageResponse<ClinicResponse> getClinics(String keyword, Pageable pageable) {
        Page<Clinic> page = clinicRepository.searchClinics(keyword, pageable);
        List<ClinicResponse> content = page.getContent().stream()
                .map(this::mapToClinicResponse)
                .toList();

        return PageResponse.<ClinicResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    public ClinicDetailResponse getClinicDetail(Long id) {
        Clinic clinic = clinicRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CLINIC_NOT_FOUND));

        List<SpecialtyResponse> specialties = specialtyRepository.findByClinics_IdAndStatus(id, "ACTIVE").stream()
                .map(s -> SpecialtyResponse.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .description(s.getDescription())
                        .status(s.getStatus())
                        .build())
                .toList();

        return ClinicDetailResponse.builder()
                .id(clinic.getId())
                .name(clinic.getName())
                .address(clinic.getAddress())
                .latitude(clinic.getLatitude())
                .longitude(clinic.getLongitude())
                .phone(clinic.getPhone())
                .description(clinic.getDescription())
                .status(clinic.getStatus())
                .specialties(specialties)
                .build();
    }

    private ClinicResponse mapToClinicResponse(Clinic clinic) {
        return ClinicResponse.builder()
                .id(clinic.getId())
                .name(clinic.getName())
                .address(clinic.getAddress())
                .latitude(clinic.getLatitude())
                .longitude(clinic.getLongitude())
                .phone(clinic.getPhone())
                .description(clinic.getDescription())
                .status(clinic.getStatus())
                .build();
    }
}
