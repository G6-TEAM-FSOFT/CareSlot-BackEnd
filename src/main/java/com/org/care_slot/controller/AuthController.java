package com.org.care_slot.controller;

import com.org.care_slot.dto.request.LoginRequest;
import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.dto.response.AuthResponse;
import com.org.care_slot.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "Các API đăng nhập, đăng ký và xác thực người dùng")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập hệ thống")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản người dùng / bệnh nhân")
    public ResponseEntity<ApiResponse<String>> register() {
        return ResponseEntity.ok(ApiResponse.success("Register endpoint stub", "USER_REGISTERED_SUCCESSFULLY"));
    }
}
