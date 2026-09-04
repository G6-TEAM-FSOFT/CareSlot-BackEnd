package com.org.care_slot.controller;

import com.org.care_slot.dto.response.ApiResponse;
import com.org.care_slot.dto.response.AppointmentResponse;
import com.org.care_slot.dto.response.VNPayIpnResponse;
import com.org.care_slot.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final VNPayService vnPayService;
    private final Long MOCK_USER_ID = 3L;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @PostMapping("/create-url")
    public ResponseEntity<ApiResponse<String>> createPaymentUrl(
            @RequestParam Long appointmentId,
            HttpServletRequest request
    ) {
        String paymentUrl = vnPayService.createPaymentUrl(appointmentId, MOCK_USER_ID, request);
        return ResponseEntity.ok(ApiResponse.success(paymentUrl));
    }

    @GetMapping("/vnpay-callback")
    public ResponseEntity<?> handleVNPayCallback(
            @RequestParam Map<String, String> params,
            HttpServletRequest request
    ) {
        AppointmentResponse response = vnPayService.handleCallback(params);

        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("text/html")) {
            String queryString = request.getQueryString();
            String redirectUrl = frontendUrl + "/payment/vnpay-callback" + (queryString != null ? "?" + queryString : "");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUrl))
                    .build();
        }

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/vnpay-ipn")
    public ResponseEntity<VNPayIpnResponse> handleVNPayIpn(
            @RequestParam Map<String, String> params
    ) {
        VNPayIpnResponse response = vnPayService.processIpn(params);
        return ResponseEntity.ok(response);
    }
}