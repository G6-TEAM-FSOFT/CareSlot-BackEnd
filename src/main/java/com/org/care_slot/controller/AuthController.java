package com.org.care_slot.controller;

import com.org.care_slot.dto.request.LoginRequest;
import com.org.care_slot.dto.request.PatientRegisterRequest;
import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.dto.response.AuthResponse;
import com.org.care_slot.entity.User;
import com.org.care_slot.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "Các API đăng nhập, làm mới token, đăng xuất và xác thực người dùng")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập hệ thống (Set HttpOnly Cookies)")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request,
                                                           HttpServletRequest httpRequest,
                                                           HttpServletResponse httpResponse) {
        AuthResponse response = authService.login(request, httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản Bệnh nhân (Tự động tạo Hồ sơ chính và Set Cookies)")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody PatientRegisterRequest request,
                                                             HttpServletRequest httpRequest,
                                                             HttpServletResponse httpResponse) {
        AuthResponse response = authService.registerPatient(request, httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký tài khoản Bệnh nhân thành công", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới Access Token & Refresh Token bằng Cookie")
    public ResponseEntity<ApiResponse<String>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        authService.refreshToken(request, response);
        return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công", "TOKEN_REFRESHED_SUCCESSFULLY"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất và xóa HttpOnly Cookies")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", "LOGGED_OUT_SUCCESSFULLY"));
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin người dùng hiện tại từ Cookie")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        AuthResponse response = authService.getCurrentUserInfo(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin người dùng thành công", response));
    }
}
