package com.org.care_slot.service.impl;

import com.org.care_slot.dto.request.ClinicUpdateRequest;
import com.org.care_slot.dto.response.ClinicDetailResponse;
import com.org.care_slot.dto.response.ClinicResponse;
import com.org.care_slot.dto.response.PageResponse;
import com.org.care_slot.dto.response.SpecialtyResponse;
import com.org.care_slot.entity.Clinic;
import com.org.care_slot.entity.Specialty;
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

    @Override
    @Transactional
    public ClinicDetailResponse updateClinic(Long clinicId, ClinicUpdateRequest request, Long staffClinicId) {
        if (staffClinicId == null || !staffClinicId.equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new AppException(ErrorCode.CLINIC_NOT_FOUND));

        clinic.setName(request.getName());
        clinic.setAddress(request.getAddress());
        if (request.getLatitude() != null) {
            clinic.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            clinic.setLongitude(request.getLongitude());
        }
        if (request.getPhone() != null) {
            clinic.setPhone(request.getPhone());
        }
        if (request.getDescription() != null) {
            clinic.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            clinic.setStatus(request.getStatus());
        }

        Clinic updated = clinicRepository.save(clinic);
        return getClinicDetail(updated.getId());
    }

    @Override
    public List<SpecialtyResponse> getClinicSpecialties(Long clinicId, Long staffClinicId) {
        if (staffClinicId == null || !staffClinicId.equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        if (!clinicRepository.existsById(clinicId)) {
            throw new AppException(ErrorCode.CLINIC_NOT_FOUND);
        }

        return specialtyRepository.findByClinics_IdAndStatus(clinicId, "ACTIVE").stream()
                .map(s -> SpecialtyResponse.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .description(s.getDescription())
                        .status(s.getStatus())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public ClinicDetailResponse addSpecialtyToClinic(Long clinicId, Long specialtyId, Long staffClinicId) {
        if (staffClinicId == null || !staffClinicId.equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new AppException(ErrorCode.CLINIC_NOT_FOUND));

        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new AppException(ErrorCode.SPECIALTY_NOT_FOUND));

        if (!"ACTIVE".equalsIgnoreCase(specialty.getStatus())) {
            throw new AppException(ErrorCode.SPECIALTY_NOT_FOUND);
        }

        clinic.getSpecialties().add(specialty);
        clinicRepository.save(clinic);

        return getClinicDetail(clinicId);
    }

    @Override
    @Transactional
    public ClinicDetailResponse removeSpecialtyFromClinic(Long clinicId, Long specialtyId, Long staffClinicId) {
        if (staffClinicId == null || !staffClinicId.equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new AppException(ErrorCode.CLINIC_NOT_FOUND));

        clinic.getSpecialties().removeIf(s -> s.getId().equals(specialtyId));
        clinicRepository.save(clinic);

        return getClinicDetail(clinicId);
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
