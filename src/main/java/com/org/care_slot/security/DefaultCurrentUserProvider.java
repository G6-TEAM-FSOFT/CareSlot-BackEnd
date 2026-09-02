package com.org.care_slot.security;

import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * Production implementation của CurrentUserProvider.
 *
 * Lưu ý kiến trúc:
 * - Module Authentication / JWT / Spring Security hoàn chỉnh thuộc phạm vi Task T-012.
 * - T-012 là integration blocker cho nguồn authenticated principal thật.
 * - Khi chưa có authenticated principal do T-012 cung cấp, provider sẽ ném lỗi 401 UNAUTHENTICATED.
 */
@Component
public class DefaultCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Long getCurrentPatientUserId() {
        // T-012 integration point: đọc principal từ SecurityContextHolder / JWT token
        // Chưa có authentication context -> trả về 401 UNAUTHENTICATED
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    }
}
