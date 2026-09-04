package com.org.care_slot.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_FOUND(1001, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(1002, "User already exists with this username or email", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(1003, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1004, "You do not have permission to perform this action", HttpStatus.FORBIDDEN),
    UNAUTHENTICATED(1005, "Unauthenticated access", HttpStatus.UNAUTHORIZED),
    PATIENT_PROFILE_NOT_FOUND(1006, "Patient profile not found", HttpStatus.NOT_FOUND),
    PRIMARY_PROFILE_ALREADY_EXISTS(1007, "Tài khoản đã có hồ sơ Chủ tài khoản, không thể tạo thêm", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_PRIMARY_PROFILE(1008, "Không thể xóa hồ sơ Chủ tài khoản", HttpStatus.BAD_REQUEST),
    CLINIC_NOT_FOUND(2001, "Clinic not found", HttpStatus.NOT_FOUND),
    FORBIDDEN_CLINIC_ACCESS(2004, "You do not have permission to manage this clinic", HttpStatus.FORBIDDEN),
    DOCTOR_NOT_FOUND(2002, "Doctor not found", HttpStatus.NOT_FOUND),
    SPECIALTY_NOT_FOUND(2003, "Specialty not found", HttpStatus.NOT_FOUND),
    SPECIALTY_NOT_BELONG_TO_CLINIC(2005, "Specialty does not belong to this clinic", HttpStatus.BAD_REQUEST),
    SLOT_NOT_FOUND(3001, "Slot not found", HttpStatus.NOT_FOUND),
    SLOT_NOT_AVAILABLE(3002, "Slot is no longer available", HttpStatus.BAD_REQUEST),
    SLOT_ALREADY_HELD(3003, "Slot is currently held by another user", HttpStatus.CONFLICT),
    SLOT_TIME_OVERLAP(3004, "Doctor schedule overlaps with an existing slot", HttpStatus.CONFLICT),
    INVALID_SLOT_TIME(3005, "Slot start time must be before end time", HttpStatus.BAD_REQUEST),
    APPOINTMENT_NOT_FOUND(4001, "Appointment not found", HttpStatus.NOT_FOUND),
    INVALID_APPOINTMENT_STATUS(4002, "Invalid appointment status transition", HttpStatus.BAD_REQUEST),
    PAYMENT_FAILED(5001, "Payment processing failed", HttpStatus.PAYMENT_REQUIRED),
    INVALID_EXCEL_FILE(6001, "Invalid Excel file format or data", HttpStatus.BAD_REQUEST),
    INVALID_PAYMENT_SIGNATURE(5002, "Invalid payment signature", HttpStatus.BAD_REQUEST),
    TRANSACTION_NOT_FOUND(5003, "Payment transaction not found", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
