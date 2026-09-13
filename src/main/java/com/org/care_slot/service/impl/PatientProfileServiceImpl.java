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
import com.org.care_slot.enums.RoleType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientProfileServiceImpl implements PatientProfileService {

    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;

    private PatientProfile findPatientProfileByIdAndUser(Long id, Long userId) {
        PatientProfile profile = patientProfileRepository.findById(id)
                .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus()))
                .orElseThrow(() -> new AppException(ErrorCode.PATIENT_PROFILE_NOT_FOUND));

        if (userId == null) {
            return profile;
        }

        if (profile.getUser() != null && profile.getUser().getId().equals(userId)) {
            return profile;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User currentUser) {
            if (currentUser.getRole() != RoleType.PATIENT) {
                return profile;
            }
        }

        throw new AppException(ErrorCode.PATIENT_PROFILE_NOT_FOUND);
    }

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
        profile.setGender(request.getGender() != null ? request.getGender().trim() : "MALE");
        profile.setPhone(request.getPhone() != null ? request.getPhone().trim() : "");
        if (request.getIdentityCard() != null) profile.setIdentityCard(request.getIdentityCard().trim());
        if (request.getCardIssueDate() != null) profile.setCardIssueDate(request.getCardIssueDate());
        if (request.getEthnicity() != null) profile.setEthnicity(request.getEthnicity().trim());
        if (request.getNationality() != null) profile.setNationality(request.getNationality().trim());
        if (request.getOccupation() != null) profile.setOccupation(request.getOccupation().trim());
        if (request.getAddress() != null) profile.setAddress(request.getAddress().trim());

        // Đồng bộ lên User
        User user = profile.getUser();
        if (user != null) {
            user.setFullName(profile.getFullName());
            if (profile.getPhone() != null && !profile.getPhone().isBlank()) {
                user.setPhone(profile.getPhone());
            }
            userRepository.save(user);
        }

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
        PatientProfile profile = findPatientProfileByIdAndUser(id, userId);
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

        // Nếu tạo hồ sơ SELF -> Cập nhật tên/sĐT cho User nếu chưa có
        if (profileType == ProfileType.PRIMARY) {
            user.setFullName(saved.getFullName());
            if (saved.getPhone() != null && !saved.getPhone().isBlank()) {
                user.setPhone(saved.getPhone());
            }
            userRepository.save(user);
        }

        return mapToResponse(saved);
    }

    @Override
    public PatientProfileResponse updatePatientProfile(Long id, Long userId, PatientProfileUpdateRequest request) {
        PatientProfile profile = findPatientProfileByIdAndUser(id, userId);

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            profile.setFullName(request.getFullName().trim());
        }
        profile.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) profile.setGender(request.getGender().trim());
        if (request.getPhone() != null) profile.setPhone(request.getPhone().trim());
        if (request.getIdentityCard() != null) profile.setIdentityCard(request.getIdentityCard().trim());
        if (request.getCardIssueDate() != null) profile.setCardIssueDate(request.getCardIssueDate());
        if (request.getEthnicity() != null) profile.setEthnicity(request.getEthnicity().trim());
        if (request.getNationality() != null) profile.setNationality(request.getNationality().trim());
        if (request.getOccupation() != null) profile.setOccupation(request.getOccupation().trim());
        if (request.getAddress() != null) profile.setAddress(request.getAddress().trim());

        // Bảo toàn thuộc tính PRIMARY nếu là hồ sơ Chủ tài khoản
        if (profile.getProfileType() == ProfileType.PRIMARY || "SELF".equalsIgnoreCase(profile.getRelationship())) {
            profile.setRelationship("SELF");
            profile.setProfileType(ProfileType.PRIMARY);
        } else if (request.getRelationship() != null && !request.getRelationship().isBlank()) {
            String rel = request.getRelationship().trim().toUpperCase();
            if ("SELF".equals(rel)) {
                boolean hasPrimary = patientProfileRepository
                        .findByUserIdAndProfileTypeAndStatus(userId, ProfileType.PRIMARY, "ACTIVE")
                        .filter(p -> !p.getId().equals(id))
                        .isPresent();
                if (hasPrimary) {
                    throw new AppException(ErrorCode.PRIMARY_PROFILE_ALREADY_EXISTS);
                }
                profile.setProfileType(ProfileType.PRIMARY);
                profile.setRelationship("SELF");
            } else {
                profile.setRelationship(rel);
                profile.setProfileType(ProfileType.FAMILY);
            }
        }

        // Nếu là hồ sơ Chủ tài khoản -> Đồng bộ dữ liệu tên & SĐT với bảng users
        if (profile.getProfileType() == ProfileType.PRIMARY) {
            User user = profile.getUser();
            if (user != null) {
                user.setFullName(profile.getFullName());
                if (profile.getPhone() != null && !profile.getPhone().isBlank()) {
                    user.setPhone(profile.getPhone());
                }
                userRepository.save(user);
            }
        }

        PatientProfile updated = patientProfileRepository.save(profile);
        return mapToResponse(updated);
    }

    @Override
    public void deletePatientProfile(Long id, Long userId) {
        PatientProfile profile = findPatientProfileByIdAndUser(id, userId);

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
