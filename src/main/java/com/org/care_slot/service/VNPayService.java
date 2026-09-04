package com.org.care_slot.service;

import com.org.care_slot.dto.response.AppointmentResponse;
import com.org.care_slot.dto.response.VNPayIpnResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface VNPayService {
    String createPaymentUrl(Long appointmentId, Long userId, HttpServletRequest request);
    AppointmentResponse handleCallback(Map<String, String> params);
    VNPayIpnResponse processIpn(Map<String, String> params);
}
