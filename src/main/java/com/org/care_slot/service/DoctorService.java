package com.org.care_slot.service;

import com.org.care_slot.dto.request.DoctorCreateRequest;
import com.org.care_slot.dto.request.DoctorUpdateRequest;
import com.org.care_slot.dto.response.DoctorDetailResponse;
import com.org.care_slot.dto.response.DoctorResponse;
import com.org.care_slot.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface DoctorService {
    PageResponse<DoctorResponse> filterDoctors(Long specialtyId, Long clinicId, String keyword, BigDecimal minFee, BigDecimal maxFee, Pageable pageable);
    DoctorDetailResponse getDoctorDetail(Long id);
    PageResponse<DoctorResponse> getDoctorsBySpecialty(Long specialtyId, Pageable pageable);

    PageResponse<DoctorResponse> getPartnerDoctors(Long clinicId, Long specialtyId, String keyword, String status, Pageable pageable, Long staffClinicId);
    DoctorDetailResponse getPartnerDoctorDetail(Long clinicId, Long doctorId, Long staffClinicId);
    DoctorDetailResponse createDoctor(Long clinicId, DoctorCreateRequest request, Long staffClinicId);
    DoctorDetailResponse updateDoctor(Long clinicId, Long doctorId, DoctorUpdateRequest request, Long staffClinicId);
    DoctorDetailResponse updateDoctorStatus(Long clinicId, Long doctorId, String status, Long staffClinicId);
}
