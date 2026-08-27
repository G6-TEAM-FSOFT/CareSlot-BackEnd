package com.org.care_slot.service;

import com.org.care_slot.dto.response.SpecialtyResponse;

import java.util.List;

public interface SpecialtyService {
    List<SpecialtyResponse> filterSpecialties(Long clinicId, String keyword);
    SpecialtyResponse getSpecialtyDetail(Long id);
    List<SpecialtyResponse> getSpecialtiesByClinic(Long clinicId);
}
