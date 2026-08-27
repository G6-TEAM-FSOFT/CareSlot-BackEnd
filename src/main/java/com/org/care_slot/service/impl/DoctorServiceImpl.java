package com.org.care_slot.service.impl;

import com.org.care_slot.dto.response.*;
import com.org.care_slot.entity.Doctor;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.repository.DoctorRepository;
import com.org.care_slot.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    @Override
    public PageResponse<DoctorResponse> filterDoctors(Long specialtyId, Long clinicId, String keyword, BigDecimal minFee, BigDecimal maxFee, Pageable pageable) {
        Page<Doctor> page = doctorRepository.filterDoctors(specialtyId, clinicId, keyword, minFee, maxFee, pageable);
        List<DoctorResponse> content = page.getContent().stream()
                .map(this::mapToDoctorResponse)
                .toList();

        return PageResponse.<DoctorResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    public DoctorDetailResponse getDoctorDetail(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

        ClinicResponse clinicResponse = null;
        if (doctor.getClinic() != null) {
            clinicResponse = ClinicResponse.builder()
                    .id(doctor.getClinic().getId())
                    .name(doctor.getClinic().getName())
                    .address(doctor.getClinic().getAddress())
                    .latitude(doctor.getClinic().getLatitude())
                    .longitude(doctor.getClinic().getLongitude())
                    .phone(doctor.getClinic().getPhone())
                    .description(doctor.getClinic().getDescription())
                    .status(doctor.getClinic().getStatus())
                    .build();
        }

        SpecialtyResponse specialtyResponse = null;
        if (doctor.getSpecialty() != null) {
            specialtyResponse = SpecialtyResponse.builder()
                    .id(doctor.getSpecialty().getId())
                    .name(doctor.getSpecialty().getName())
                    .description(doctor.getSpecialty().getDescription())
                    .status(doctor.getSpecialty().getStatus())
                    .build();
        }

        return DoctorDetailResponse.builder()
                .id(doctor.getId())
                .fullName(doctor.getFullName())
                .title(doctor.getTitle())
                .bio(doctor.getBio())
                .avatarUrl(doctor.getAvatarUrl())
                .consultationFee(doctor.getConsultationFee())
                .status(doctor.getStatus())
                .clinic(clinicResponse)
                .specialty(specialtyResponse)
                .build();
    }

    @Override
    public PageResponse<DoctorResponse> getDoctorsBySpecialty(Long specialtyId, Pageable pageable) {
        Page<Doctor> page = doctorRepository.findBySpecialtyIdAndStatus(specialtyId, "ACTIVE", pageable);
        List<DoctorResponse> content = page.getContent().stream()
                .map(this::mapToDoctorResponse)
                .toList();

        return PageResponse.<DoctorResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private DoctorResponse mapToDoctorResponse(Doctor doctor) {
        return DoctorResponse.builder()
                .id(doctor.getId())
                .fullName(doctor.getFullName())
                .title(doctor.getTitle())
                .avatarUrl(doctor.getAvatarUrl())
                .consultationFee(doctor.getConsultationFee())
                .clinicId(doctor.getClinic() != null ? doctor.getClinic().getId() : null)
                .clinicName(doctor.getClinic() != null ? doctor.getClinic().getName() : null)
                .specialtyId(doctor.getSpecialty() != null ? doctor.getSpecialty().getId() : null)
                .specialtyName(doctor.getSpecialty() != null ? doctor.getSpecialty().getName() : null)
                .status(doctor.getStatus())
                .build();
    }
}
