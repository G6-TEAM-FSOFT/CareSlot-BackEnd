package com.org.care_slot.service;

import com.org.care_slot.dto.request.PatientProfileCreateRequest;
import com.org.care_slot.dto.request.PatientProfileUpdateRequest;
import com.org.care_slot.dto.request.UpdatePrimaryProfileRequest;
import com.org.care_slot.dto.response.PatientProfileResponse;

import java.util.List;

public interface PatientProfileService {
    PatientProfileResponse getMyPrimaryProfile(Long userId);
    PatientProfileResponse updateMyPrimaryProfile(Long userId, UpdatePrimaryProfileRequest request);

    List<PatientProfileResponse> getPatientProfiles(Long userId, String keyword);
    PatientProfileResponse getPatientProfileDetail(Long id, Long userId);
    PatientProfileResponse createPatientProfile(Long userId, PatientProfileCreateRequest request);
    PatientProfileResponse updatePatientProfile(Long id, Long userId, PatientProfileUpdateRequest request);
    void deletePatientProfile(Long id, Long userId);
}
