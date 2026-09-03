package com.org.care_slot.service;

import com.org.care_slot.dto.request.ClinicUpdateRequest;
import com.org.care_slot.dto.response.ClinicDetailResponse;
import com.org.care_slot.dto.response.ClinicResponse;
import com.org.care_slot.dto.response.PageResponse;
import com.org.care_slot.dto.response.SpecialtyResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClinicService {
    PageResponse<ClinicResponse> getClinics(String keyword, Pageable pageable);
    ClinicDetailResponse getClinicDetail(Long id);
    ClinicDetailResponse updateClinic(Long clinicId, ClinicUpdateRequest request, Long staffClinicId);
    List<SpecialtyResponse> getClinicSpecialties(Long clinicId, Long staffClinicId);
    ClinicDetailResponse addSpecialtyToClinic(Long clinicId, Long specialtyId, Long staffClinicId);
    ClinicDetailResponse removeSpecialtyFromClinic(Long clinicId, Long specialtyId, Long staffClinicId);
}
