package com.org.care_slot.service.impl;

import com.org.care_slot.dto.request.PatientProfileCreateRequest;
import com.org.care_slot.dto.request.PatientProfileUpdateRequest;
import com.org.care_slot.dto.request.UpdatePrimaryProfileRequest;
import com.org.care_slot.dto.response.PatientProfileResponse;
import com.org.care_slot.entity.PatientProfile;
import com.org.care_slot.entity.User;
import com.org.care_slot.enums.ProfileType;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.repository.PatientProfileRepository;
import com.org.care_slot.repository.UserRepository;
import com.org.care_slot.service.PatientProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientProfileServiceImpl implements PatientProfileService {

    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public PatientProfileResponse getMyPrimaryProfile(Long userId) {
        PatientProfile profile = patientProfileRepository.findByUserIdAndProfileType(userId, ProfileType.PRIMARY)
                .orElseThrow(() -> new AppException(ErrorCode.PATIENT_PROFILE_NOT_FOUND));
        return mapToResponse(profile);
    }

    @Override
    public PatientProfileResponse updateMyPrimaryProfile(Long userId, UpdatePrimaryProfileRequest request) {
        PatientProfile profile = patientProfileRepository.findByUserIdAndProfileType(userId, ProfileType.PRIMARY)
                .orElseThrow(() -> new AppException(ErrorCode.PATIENT_PROFILE_NOT_FOUND));

        profile.setFullName(request.getFullName().trim());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender().trim());
        profile.setPhone(request.getPhone().trim());

        // Không thay đổi: user, profileType, relationship, status
        PatientProfile updated = patientProfileRepository.save(profile);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientProfileResponse> getPatientProfiles(Long userId, String keyword) {
        return patientProfileRepository.findByUserIdAndKeyword(userId, keyword).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PatientProfileResponse getPatientProfileDetail(Long id, Long userId) {
        PatientProfile profile = patientProfileRepository.findByIdAndUserIdAndStatus(id, userId, "ACTIVE")
                .orElseThrow(() -> new AppException(ErrorCode.PATIENT_PROFILE_NOT_FOUND));
        return mapToResponse(profile);
    }

    @Override
    public PatientProfileResponse createPatientProfile(Long userId, PatientProfileCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String rel = request.getRelationship() != null ? request.getRelationship().trim().toUpperCase() : "SELF";
        ProfileType profileType = "SELF".equals(rel) ? ProfileType.PRIMARY : ProfileType.FAMILY;

        PatientProfile profile = PatientProfile.builder()
                .user(user)
                .profileType(profileType)
                .fullName(request.getFullName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .relationship(rel)
                .status("ACTIVE")
                .build();

        PatientProfile saved = patientProfileRepository.save(profile);
        return mapToResponse(saved);
    }

    @Override
    public PatientProfileResponse updatePatientProfile(Long id, Long userId, PatientProfileUpdateRequest request) {
        PatientProfile profile = patientProfileRepository.findByIdAndUserIdAndStatus(id, userId, "ACTIVE")
                .orElseThrow(() -> new AppException(ErrorCode.PATIENT_PROFILE_NOT_FOUND));

        profile.setFullName(request.getFullName());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());
        profile.setPhone(request.getPhone());
        if (request.getRelationship() != null) {
            String rel = request.getRelationship().trim().toUpperCase();
            profile.setRelationship(rel);
            profile.setProfileType("SELF".equals(rel) ? ProfileType.PRIMARY : ProfileType.FAMILY);
        }

        PatientProfile updated = patientProfileRepository.save(profile);
        return mapToResponse(updated);
    }

    @Override
    public void deletePatientProfile(Long id, Long userId) {
        PatientProfile profile = patientProfileRepository.findByIdAndUserIdAndStatus(id, userId, "ACTIVE")
                .orElseThrow(() -> new AppException(ErrorCode.PATIENT_PROFILE_NOT_FOUND));
        profile.setStatus("INACTIVE");
        patientProfileRepository.save(profile);
    }

    private PatientProfileResponse mapToResponse(PatientProfile profile) {
        return PatientProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser() != null ? profile.getUser().getId() : null)
                .profileType(profile.getProfileType())
                .fullName(profile.getFullName())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .phone(profile.getPhone())
                .relationship(profile.getRelationship())
                .status(profile.getStatus())
                .build();
    }
}
