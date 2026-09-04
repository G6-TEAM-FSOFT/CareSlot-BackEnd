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
        if (request.getIdentityCard() != null) profile.setIdentityCard(request.getIdentityCard().trim());
        if (request.getCardIssueDate() != null) profile.setCardIssueDate(request.getCardIssueDate());
        if (request.getEthnicity() != null) profile.setEthnicity(request.getEthnicity().trim());
        if (request.getNationality() != null) profile.setNationality(request.getNationality().trim());
        if (request.getOccupation() != null) profile.setOccupation(request.getOccupation().trim());
        if (request.getAddress() != null) profile.setAddress(request.getAddress().trim());

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

        String rel = request.getRelationship() != null ? request.getRelationship().trim().toUpperCase() : "FAMILY";
        
        // Chặn tạo nhiều hơn 1 hồ sơ Chủ tài khoản (SELF / PRIMARY) cho 1 tài khoản
        if ("SELF".equals(rel)) {
            boolean hasPrimary = patientProfileRepository
                    .findByUserIdAndProfileTypeAndStatus(userId, ProfileType.PRIMARY, "ACTIVE")
                    .isPresent();
            if (hasPrimary) {
                throw new AppException(ErrorCode.PRIMARY_PROFILE_ALREADY_EXISTS);
            }
        }

        ProfileType profileType = "SELF".equals(rel) ? ProfileType.PRIMARY : ProfileType.FAMILY;

        PatientProfile profile = PatientProfile.builder()
                .user(user)
                .profileType(profileType)
                .fullName(request.getFullName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .identityCard(request.getIdentityCard())
                .cardIssueDate(request.getCardIssueDate())
                .ethnicity(request.getEthnicity() != null && !request.getEthnicity().isBlank() ? request.getEthnicity() : "Kinh")
                .nationality(request.getNationality() != null && !request.getNationality().isBlank() ? request.getNationality() : "Việt Nam")
                .occupation(request.getOccupation())
                .address(request.getAddress())
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
        if (request.getIdentityCard() != null) profile.setIdentityCard(request.getIdentityCard());
        if (request.getCardIssueDate() != null) profile.setCardIssueDate(request.getCardIssueDate());
        if (request.getEthnicity() != null) profile.setEthnicity(request.getEthnicity());
        if (request.getNationality() != null) profile.setNationality(request.getNationality());
        if (request.getOccupation() != null) profile.setOccupation(request.getOccupation());
        if (request.getAddress() != null) profile.setAddress(request.getAddress());

        if (request.getRelationship() != null) {
            String rel = request.getRelationship().trim().toUpperCase();
            if ("SELF".equals(rel) && profile.getProfileType() != ProfileType.PRIMARY) {
                boolean hasPrimary = patientProfileRepository
                        .findByUserIdAndProfileTypeAndStatus(userId, ProfileType.PRIMARY, "ACTIVE")
                        .isPresent();
                if (hasPrimary) {
                    throw new AppException(ErrorCode.PRIMARY_PROFILE_ALREADY_EXISTS);
                }
            }
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

        // Không cho phép xóa hồ sơ Chủ tài khoản
        if (profile.getProfileType() == ProfileType.PRIMARY || "SELF".equalsIgnoreCase(profile.getRelationship())) {
            throw new AppException(ErrorCode.CANNOT_DELETE_PRIMARY_PROFILE);
        }

        // Nếu hồ sơ chưa từng phát sinh cuộc hẹn nào -> Xóa vĩnh viễn khỏi Database (Hard delete)
        // Nếu đã có cuộc hẹn -> Ẩn bằng Soft delete để bảo toàn dữ liệu lịch sử khám
        if (profile.getAppointments() == null || profile.getAppointments().isEmpty()) {
            patientProfileRepository.delete(profile);
        } else {
            profile.setStatus("INACTIVE");
            patientProfileRepository.save(profile);
        }
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
                .identityCard(profile.getIdentityCard())
                .cardIssueDate(profile.getCardIssueDate())
                .ethnicity(profile.getEthnicity())
                .nationality(profile.getNationality())
                .occupation(profile.getOccupation())
                .address(profile.getAddress())
                .relationship(profile.getRelationship())
                .status(profile.getStatus())
                .build();
    }
}
