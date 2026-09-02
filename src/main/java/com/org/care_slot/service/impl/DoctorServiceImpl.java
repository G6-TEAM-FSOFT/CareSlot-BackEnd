package com.org.care_slot.service.impl;

import com.org.care_slot.dto.request.DoctorCreateRequest;
import com.org.care_slot.dto.request.DoctorUpdateRequest;
import com.org.care_slot.dto.response.*;
import com.org.care_slot.entity.Clinic;
import com.org.care_slot.entity.Doctor;
import com.org.care_slot.entity.Specialty;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.repository.ClinicRepository;
import com.org.care_slot.repository.DoctorRepository;
import com.org.care_slot.repository.SpecialtyRepository;
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
    private final ClinicRepository clinicRepository;
    private final SpecialtyRepository specialtyRepository;

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

    @Override
    public PageResponse<DoctorResponse> getPartnerDoctors(Long clinicId, Long specialtyId, String keyword, String status, Pageable pageable, Long staffClinicId) {
        if (staffClinicId == null || !staffClinicId.equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        Page<Doctor> page = doctorRepository.findPartnerDoctors(clinicId, specialtyId, keyword, status, pageable);
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
    public DoctorDetailResponse getPartnerDoctorDetail(Long clinicId, Long doctorId, Long staffClinicId) {
        if (staffClinicId == null || !staffClinicId.equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

        if (!doctor.getClinic().getId().equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        return getDoctorDetail(doctorId);
    }

    @Override
    @Transactional
    public DoctorDetailResponse createDoctor(Long clinicId, DoctorCreateRequest request, Long staffClinicId) {
        if (staffClinicId == null || !staffClinicId.equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new AppException(ErrorCode.CLINIC_NOT_FOUND));

        Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new AppException(ErrorCode.SPECIALTY_NOT_FOUND));

        boolean belongsToClinic = clinic.getSpecialties().stream()
                .anyMatch(s -> s.getId().equals(specialty.getId()));
        if (!belongsToClinic) {
            throw new AppException(ErrorCode.SPECIALTY_NOT_BELONG_TO_CLINIC);
        }

        Doctor doctor = Doctor.builder()
                .clinic(clinic)
                .specialty(specialty)
                .fullName(request.getFullName())
                .title(request.getTitle())
                .bio(request.getBio())
                .avatarUrl(request.getAvatarUrl())
                .consultationFee(request.getConsultationFee() != null ? request.getConsultationFee() : BigDecimal.ZERO)
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();

        Doctor saved = doctorRepository.save(doctor);
        return getDoctorDetail(saved.getId());
    }

    @Override
    @Transactional
    public DoctorDetailResponse updateDoctor(Long clinicId, Long doctorId, DoctorUpdateRequest request, Long staffClinicId) {
        if (staffClinicId == null || !staffClinicId.equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

        if (!doctor.getClinic().getId().equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        if (request.getSpecialtyId() != null) {
            Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                    .orElseThrow(() -> new AppException(ErrorCode.SPECIALTY_NOT_FOUND));

            boolean belongsToClinic = doctor.getClinic().getSpecialties().stream()
                    .anyMatch(s -> s.getId().equals(specialty.getId()));
            if (!belongsToClinic) {
                throw new AppException(ErrorCode.SPECIALTY_NOT_BELONG_TO_CLINIC);
            }
            doctor.setSpecialty(specialty);
        }

        if (request.getFullName() != null) {
            doctor.setFullName(request.getFullName());
        }
        if (request.getTitle() != null) {
            doctor.setTitle(request.getTitle());
        }
        if (request.getBio() != null) {
            doctor.setBio(request.getBio());
        }
        if (request.getAvatarUrl() != null) {
            doctor.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getConsultationFee() != null) {
            doctor.setConsultationFee(request.getConsultationFee());
        }
        if (request.getStatus() != null) {
            doctor.setStatus(request.getStatus());
        }

        Doctor updated = doctorRepository.save(doctor);
        return getDoctorDetail(updated.getId());
    }

    @Override
    @Transactional
    public DoctorDetailResponse updateDoctorStatus(Long clinicId, Long doctorId, String status, Long staffClinicId) {
        if (staffClinicId == null || !staffClinicId.equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

        if (!doctor.getClinic().getId().equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        doctor.setStatus(status);
        Doctor updated = doctorRepository.save(doctor);
        return getDoctorDetail(updated.getId());
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
