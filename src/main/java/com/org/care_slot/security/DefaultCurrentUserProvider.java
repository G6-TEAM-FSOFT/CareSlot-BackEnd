package com.org.care_slot.security;

import com.org.care_slot.entity.User;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Production implementation của CurrentUserProvider.
 * Lấy authenticated user principal từ Spring SecurityContextHolder đã được JwtAuthenticationFilter populate.
 */
@Component
public class DefaultCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Long getCurrentPatientUserId() {
        User user = getCurrentUser();
        return user.getId();
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }

    @Override
    public Long getCurrentClinicId() {
        User user = getCurrentUser();
        if (user.getClinic() != null) {
            return user.getClinic().getId();
        }
        throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
    }
}
