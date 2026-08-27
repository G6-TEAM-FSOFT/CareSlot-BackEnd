package com.org.care_slot.controller;

import com.org.care_slot.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication API", description = "Các API đăng nhập, đăng ký và xác thực người dùng")
public class AuthController {

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập hệ thống")
    public ResponseEntity<ApiResponse<String>> login() {
        return ResponseEntity.ok(ApiResponse.success("Login endpoint stub", "JWT_TOKEN_PLACEHOLDER"));
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản người dùng / bệnh nhân")
    public ResponseEntity<ApiResponse<String>> register() {
        return ResponseEntity.ok(ApiResponse.success("Register endpoint stub", "USER_REGISTERED_SUCCESSFULLY"));
    }
}
