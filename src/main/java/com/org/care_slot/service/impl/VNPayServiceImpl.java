package com.org.care_slot.service.impl;

import com.org.care_slot.dto.response.PatientAppointmentResponse;
import com.org.care_slot.dto.response.VNPayIpnResponse;
import com.org.care_slot.entity.*;
import com.org.care_slot.enums.*;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.repository.*;
import com.org.care_slot.service.BookingLogService;
import com.org.care_slot.service.PatientAppointmentMapper;
import com.org.care_slot.service.SlotAllocationService;
import com.org.care_slot.service.VNPayService;
import com.org.care_slot.util.VNPayUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements VNPayService {
    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotRepository appointmentSlotRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final InvoiceRepository invoiceRepository;
    private final BookingLogService bookingLogService;
    private final SlotAllocationService slotAllocationService;
    private final PatientAppointmentMapper patientAppointmentMapper;
    private final EntityManager entityManager;

    @Value("${vnpay.tmn-code}") private String tmnCode;
    @Value("${vnpay.hash-secret}") private String hashSecret;
    @Value("${vnpay.pay-url}") private String payUrl;
    @Value("${vnpay.return-url}") private String returnUrl;
    private static final DateTimeFormatter PAYMENT_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional
    public String createPaymentUrl(Long appointmentId, Long userId, HttpServletRequest request) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));
        if (appointment.getPatientProfile() == null || appointment.getPatientProfile().getUser() == null
                || !appointment.getPatientProfile().getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        lockAndRefresh(appointment);
        AppointmentSlot slot = appointment.getSlot();
        LocalDateTime now = SlotAllocationService.now();
        if (!ownsLiveHold(appointment, slot, now)) {
            throw new AppException(ErrorCode.INVALID_APPOINTMENT_STATUS,
                    "Thời gian giữ chỗ đã hết hoặc lịch hẹn không còn chờ thanh toán.");
        }

        // Reopening the dialog neither extends the hold nor creates parallel payment attempts.
        PaymentTransaction transaction = paymentTransactionRepository
                .findFirstByAppointmentIdAndStatusOrderByCreatedAtDesc(appointmentId, PaymentStatus.PENDING)
                .orElseGet(() -> paymentTransactionRepository.save(PaymentTransaction.builder()
                        .txnRef("CS" + UUID.randomUUID().toString().replace("-", ""))
                        .appointment(appointment).amount(appointment.getDepositAmount())
                        .paymentProvider("VNPAY").status(PaymentStatus.PENDING).createdAt(now).build()));

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", transaction.getAmount().movePointRight(2).toBigIntegerExact().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", transaction.getTxnRef());
        params.put("vnp_OrderInfo", "Thanh toan tien coc booking " + appointment.getBookingCode());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));
        params.put("vnp_CreateDate", PAYMENT_TIME.format(transaction.getCreatedAt()));
        params.put("vnp_ExpireDate", PAYMENT_TIME.format(slot.getHoldExpiresAt()));
        String query = canonicalFields(params);
        return payUrl + "?" + query + "&vnp_SecureHash=" + VNPayUtil.hmacSHA512(hashSecret, query);
    }

    @Override
    @Transactional
    public PatientAppointmentResponse handleCallback(Map<String, String> params) {
        if (!validSignature(params)) throw new AppException(ErrorCode.INVALID_PAYMENT_SIGNATURE);
        PaymentTransaction transaction = paymentTransactionRepository.findByTxnRef(params.get("vnp_TxnRef"))
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));
        lockAndRefresh(transaction.getAppointment());
        entityManager.refresh(transaction);
        if (!validAmount(params, transaction)) {
            throw new AppException(ErrorCode.PAYMENT_FAILED, "Số tiền thanh toán không khớp với giao dịch.");
        }
        applyPaymentResult(transaction, params);
        return patientAppointmentMapper.toResponse(transaction.getAppointment());
    }

    @Override
    @Transactional
    public VNPayIpnResponse processIpn(Map<String, String> params) {
        if (!validSignature(params)) return ipn("97", "Invalid Checksum");
        Optional<PaymentTransaction> found = paymentTransactionRepository.findByTxnRef(params.get("vnp_TxnRef"));
        if (found.isEmpty()) return ipn("01", "Order not found");
        PaymentTransaction transaction = found.get();
        lockAndRefresh(transaction.getAppointment());
        entityManager.refresh(transaction);
        if (!validAmount(params, transaction)) return ipn("04", "Invalid Amount");
        if (transaction.getStatus() != PaymentStatus.PENDING) return ipn("02", "Order already confirmed");
        applyPaymentResult(transaction, params);
        return ipn("00", "Confirm Success");
    }

    private void lockAndRefresh(Appointment appointment) {
        slotAllocationService.lockClinic(appointment.getSlot().getDoctor().getClinic().getId());
        // Current reads under the lock shared with allocation, cancellation, check-in and expiry.
        entityManager.refresh(appointment);
        entityManager.refresh(appointment.getSlot());
    }

    private boolean ownsLiveHold(Appointment appointment, AppointmentSlot slot, LocalDateTime now) {
        return appointment.getStatus() == AppointmentStatus.PENDING_PAYMENT
                && slot.getStatus() == SlotStatus.HELD
                && slot.getHoldExpiresAt() != null && now.isBefore(slot.getHoldExpiresAt())
                && now.isBefore(slot.getAppointmentDate().atTime(slot.getStartTime()));
    }

    private void applyPaymentResult(PaymentTransaction transaction, Map<String, String> params) {
        if (transaction.getStatus() != PaymentStatus.PENDING) return;
        Appointment appointment = transaction.getAppointment();
        AppointmentSlot slot = appointment.getSlot();
        LocalDateTime now = SlotAllocationService.now();
        String previous = appointment.getStatus().name();
        boolean paid = "00".equals(params.get("vnp_ResponseCode"))
                && "00".equals(params.get("vnp_TransactionStatus"));
        transaction.setResponseCode(params.get("vnp_ResponseCode"));
        transaction.setTransactionNo(params.get("vnp_TransactionNo"));
        transaction.setBankCode(params.get("vnp_BankCode"));
        transaction.setPaymentTime(parsePaymentTime(params.get("vnp_PayDate"), now));
        transaction.setStatus(paid ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        paymentTransactionRepository.save(transaction);

        if (paid) {
            if (ownsLiveHold(appointment, slot, now)) {
                slot.setStatus(SlotStatus.BOOKED);
                slot.setHeldAt(null);
                slot.setHoldExpiresAt(null);
                appointment.setStatus(AppointmentStatus.CONFIRMED);
                appointment.setApprovedAt(now);
                appointmentSlotRepository.save(slot);
                appointmentRepository.save(appointment);
            } else if (appointment.getStatus() == AppointmentStatus.PENDING_PAYMENT) {
                // Record late funds without reclaiming a slot released to another appointment.
                appointment.setStatus(AppointmentStatus.EXPIRED);
                if (slot.getStatus() == SlotStatus.HELD) {
                    slot.setStatus(now.isBefore(slot.getAppointmentDate().atTime(slot.getStartTime()))
                            ? SlotStatus.AVAILABLE : SlotStatus.OVER_DATE);
                    slot.setHeldAt(null);
                    slot.setHoldExpiresAt(null);
                    appointmentSlotRepository.save(slot);
                }
                appointmentRepository.save(appointment);
            }
            invoiceRepository.findByAppointmentId(appointment.getId()).ifPresent(invoice -> {
                invoice.setStatus("PAID");
                invoice.setPaidAt(transaction.getPaymentTime());
                invoiceRepository.save(invoice);
            });
            boolean confirmed = Set.of(AppointmentStatus.CONFIRMED, AppointmentStatus.CHECKED_IN,
                    AppointmentStatus.COMPLETED).contains(appointment.getStatus());
            bookingLogService.logEvent(appointment, previous, appointment.getStatus().name(),
                    confirmed ? "PAYMENT_SUCCESS" : "PAYMENT_REVIEW_REQUIRED",
                    confirmed ? "Thanh toán cọc thành công qua VNPay."
                            : "Đã nhận tiền cọc nhưng lịch hẹn không còn hiệu lực; cần đối soát tại cơ sở.", "SYSTEM");
        } else {
            // Failed attempts cannot undo a success or release another patient's hold.
            bookingLogService.logEvent(appointment, previous, appointment.getStatus().name(), "PAYMENT_FAILED",
                    "Giao dịch chưa thành công; có thể thử lại nếu thời gian giữ chỗ còn hiệu lực.", "SYSTEM");
        }
    }

    private boolean validSignature(Map<String, String> params) {
        String supplied = params.get("vnp_SecureHash");
        return supplied != null && tmnCode.equals(params.get("vnp_TmnCode"))
                && VNPayUtil.hmacSHA512(hashSecret, canonicalFields(params)).equalsIgnoreCase(supplied);
    }

    private boolean validAmount(Map<String, String> params, PaymentTransaction transaction) {
        try {
            return transaction.getAmount().movePointRight(2).toBigIntegerExact()
                    .equals(new java.math.BigInteger(params.get("vnp_Amount")));
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private String canonicalFields(Map<String, String> params) {
        return new TreeMap<>(params).entrySet().stream()
                .filter(e -> e.getKey().startsWith("vnp_") && !e.getKey().equals("vnp_SecureHash")
                        && !e.getKey().equals("vnp_SecureHashType") && e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.US_ASCII) + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.US_ASCII))
                .collect(Collectors.joining("&"));
    }

    private LocalDateTime parsePaymentTime(String value, LocalDateTime fallback) {
        try { return LocalDateTime.parse(value, PAYMENT_TIME); }
        catch (DateTimeParseException | NullPointerException exception) { return fallback; }
    }

    private VNPayIpnResponse ipn(String code, String message) {
        return VNPayIpnResponse.builder().rspCode(code).message(message).build();
    }
}
