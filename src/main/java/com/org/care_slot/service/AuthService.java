package com.org.care_slot.service;

import com.org.care_slot.dto.request.LoginRequest;
import com.org.care_slot.dto.response.AuthResponse;
import com.org.care_slot.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.org.care_slot.dto.request.PatientRegisterRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);
    AuthResponse registerPatient(PatientRegisterRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);
    void refreshToken(HttpServletRequest request, HttpServletResponse response);
    void logout(HttpServletRequest request, HttpServletResponse response);
    AuthResponse getCurrentUserInfo(User currentUser);
}
