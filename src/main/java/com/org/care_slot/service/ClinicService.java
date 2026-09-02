package com.org.care_slot.service;

import com.org.care_slot.dto.request.ClinicUpdateRequest;
import com.org.care_slot.dto.response.ClinicDetailResponse;
import com.org.care_slot.dto.response.ClinicResponse;
import com.org.care_slot.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ClinicService {
    PageResponse<ClinicResponse> getClinics(String keyword, Pageable pageable);
    ClinicDetailResponse getClinicDetail(Long id);
    ClinicDetailResponse updateClinic(Long clinicId, ClinicUpdateRequest request, Long staffClinicId);
}
