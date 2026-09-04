package com.org.care_slot.service.impl;

import com.org.care_slot.dto.response.AppointmentResponse;
import com.org.care_slot.entity.Appointment;
import com.org.care_slot.entity.AppointmentSlot;
import com.org.care_slot.entity.PaymentTransaction;
import com.org.care_slot.enums.AppointmentStatus;
import com.org.care_slot.enums.PaymentStatus;
import com.org.care_slot.enums.SlotStatus;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.repository.AppointmentRepository;
import com.org.care_slot.repository.AppointmentSlotRepository;
import com.org.care_slot.repository.PaymentTransactionRepository;
import com.org.care_slot.service.VNPayService;
import com.org.care_slot.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class VNPayServiceImpl implements VNPayService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotRepository appointmentSlotRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final com.org.care_slot.service.BookingLogService bookingLogService;

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.pay-url}")
    private String payUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    @Value("${vnpay.hold-timeout-minutes:10}")
    private long holdTimeoutMinutes;

    @Override
    public String createPaymentUrl(Long appointmentId, Long userId, HttpServletRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        if (!appointment.getPatientProfile().getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            throw new AppException(ErrorCode.INVALID_APPOINTMENT_STATUS);
        }

        String txnRef = "CS" + System.currentTimeMillis() + VNPayUtil.getRandomNumber(4);

        // Lưu giao dịch PENDING
        PaymentTransaction transaction = PaymentTransaction.builder()
                .txnRef(txnRef)
                .appointment(appointment)
                .amount(appointment.getDepositAmount())
                .paymentProvider("VNPAY")
                .status(PaymentStatus.PENDING)
                .build();
        paymentTransactionRepository.save(transaction);

        // Build VNPay params
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        long amountInCents = appointment.getDepositAmount().longValue() * 100;
        vnpParams.put("vnp_Amount", String.valueOf(amountInCents));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", "Thanh toan tien coc booking " + appointment.getBookingCode());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime now = LocalDateTime.now();
        vnpParams.put("vnp_CreateDate", formatter.format(now));
        vnpParams.put("vnp_ExpireDate", formatter.format(now.plusMinutes(holdTimeoutMinutes)));

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnpSecureHash = VNPayUtil.hmacSHA512(hashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;

        return payUrl + "?" + queryUrl;
    }

    @Override
    public AppointmentResponse handleCallback(Map<String, String> params) {
        String vnpSecureHash = params.get("vnp_SecureHash");
        Map<String, String> fields = new HashMap<>(params);
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String signValue = VNPayUtil.hmacSHA512(hashSecret, hashData.toString());
        if (!signValue.equalsIgnoreCase(vnpSecureHash)) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_SIGNATURE);
        }

        String txnRef = params.get("vnp_TxnRef");
        PaymentTransaction transaction = paymentTransactionRepository.findByTxnRef(txnRef)
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        Appointment appointment = transaction.getAppointment();
        AppointmentSlot slot = appointment.getSlot();

        // Check Idempotency: nếu giao dịch không còn PENDING thì trả về kết quả luôn
        if (transaction.getStatus() != PaymentStatus.PENDING) {
            return mapToResponse(appointment);
        }

        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String bankCode = params.get("vnp_BankCode");

        transaction.setResponseCode(responseCode);
        transaction.setTransactionNo(transactionNo);
        transaction.setBankCode(bankCode);
        transaction.setPaymentTime(LocalDateTime.now());

        if ("00".equals(responseCode)) {
            transaction.setStatus(PaymentStatus.SUCCESS);
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            if (slot != null) {
                slot.setStatus(SlotStatus.BOOKED);
                appointmentSlotRepository.save(slot);
            }
        } else {
            transaction.setStatus(PaymentStatus.FAILED);
            appointment.setStatus(AppointmentStatus.EXPIRED);
            if (slot != null) {
                slot.setStatus(SlotStatus.AVAILABLE);
                slot.setHeldAt(null);
                slot.setHoldExpiresAt(null);
                appointmentSlotRepository.save(slot);
            }
        }

        paymentTransactionRepository.save(transaction);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        if ("00".equals(responseCode)) {
            bookingLogService.logEvent(savedAppointment, "PENDING_PAYMENT", "CONFIRMED", "PAYMENT_SUCCESS", "Thanh toán cọc thành công qua VNPAY, lịch hẹn đã được xác nhận", "PATIENT");
        } else {
            bookingLogService.logEvent(savedAppointment, "PENDING_PAYMENT", "EXPIRED", "PAYMENT_FAILED", "Thanh toán tiền cọc thất bại qua VNPAY, lịch hẹn đã hết hạn", "PATIENT");
        }

        return mapToResponse(savedAppointment);
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        AppointmentSlot slot = appointment.getSlot();
        var profile = appointment.getPatientProfile();

        return AppointmentResponse.builder()
                .id(appointment.getId())
                .bookingCode(appointment.getBookingCode())
                .patientProfileId(profile != null ? profile.getId() : null)
                .patientName(profile != null ? profile.getFullName() : null)
                .doctorId(slot != null && slot.getDoctor() != null ? slot.getDoctor().getId() : null)
                .doctorName(slot != null && slot.getDoctor() != null ? slot.getDoctor().getFullName() : null)
                .clinicName(slot != null && slot.getDoctor() != null && slot.getDoctor().getClinic() != null ? slot.getDoctor().getClinic().getName() : null)
                .specialtyName(slot != null && slot.getDoctor() != null && slot.getDoctor().getSpecialty() != null ? slot.getDoctor().getSpecialty().getName() : null)
                .slotId(slot != null ? slot.getId() : null)
                .appointmentDate(slot != null ? slot.getAppointmentDate() : null)
                .startTime(slot != null ? slot.getStartTime() : null)
                .endTime(slot != null ? slot.getEndTime() : null)
                .roomName(slot != null ? slot.getRoomName() : null)
                .symptomNote(appointment.getSymptomNote())
                .consultationFee(appointment.getConsultationFee())
                .depositAmount(appointment.getDepositAmount())
                .status(appointment.getStatus())
                .createdAt(appointment.getCreatedAt())
                .approvedAt(appointment.getApprovedAt())
                .rejectedAt(appointment.getRejectedAt())
                .cancelledAt(appointment.getCancelledAt())
                .checkedInAt(appointment.getCheckedInAt())
                .build();
    }
}
