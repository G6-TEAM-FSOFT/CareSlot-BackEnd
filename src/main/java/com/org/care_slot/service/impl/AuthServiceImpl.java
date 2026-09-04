package com.org.care_slot.service.impl;

import com.org.care_slot.dto.request.LoginRequest;
import com.org.care_slot.dto.response.AuthResponse;
import com.org.care_slot.entity.User;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.repository.UserRepository;
import com.org.care_slot.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Validate password against passwordHash or demo password placeholders
        if (!isPasswordMatch(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        Long clinicId = user.getClinic() != null ? user.getClinic().getId() : null;
        String clinicName = user.getClinic() != null ? user.getClinic().getName() : null;

        String generatedToken = "care_slot_token_" + user.getId() + "_" + System.currentTimeMillis();

        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .clinicId(clinicId)
                .clinicName(clinicName)
                .token(generatedToken)
                .build();
    }

    private boolean isPasswordMatch(String rawPassword, String storedHash) {
        if (storedHash == null || rawPassword == null) {
            return false;
        }
        if ("DEMO_PASSWORD_HASH".equals(storedHash)) {
            return true;
        }
        return rawPassword.equals(storedHash);
    }
}
