package com.org.care_slot.service;

import com.org.care_slot.dto.request.LoginRequest;
import com.org.care_slot.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
}
