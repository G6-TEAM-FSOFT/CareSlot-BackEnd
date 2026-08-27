package com.org.care_slot.service;

import com.org.care_slot.dto.response.DoctorDetailResponse;
import com.org.care_slot.dto.response.DoctorResponse;
import com.org.care_slot.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface DoctorService {
    PageResponse<DoctorResponse> filterDoctors(Long specialtyId, Long clinicId, String keyword, BigDecimal minFee, BigDecimal maxFee, Pageable pageable);
    DoctorDetailResponse getDoctorDetail(Long id);
    PageResponse<DoctorResponse> getDoctorsBySpecialty(Long specialtyId, Pageable pageable);
}
