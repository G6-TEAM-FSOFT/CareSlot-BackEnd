package com.org.care_slot.security;

public interface CurrentUserProvider {

    /**
     * Yêu cầu user hiện tại phải đăng nhập và có role PATIENT.
     * Trả về userId nếu hợp lệ.
     * Ném AppException(ErrorCode.UNAUTHENTICATED) nếu chưa đăng nhập (HTTP 401).
     * Ném AppException(ErrorCode.UNAUTHORIZED) nếu role không phải PATIENT (HTTP 403).
     */
    Long getCurrentPatientUserId();
}
