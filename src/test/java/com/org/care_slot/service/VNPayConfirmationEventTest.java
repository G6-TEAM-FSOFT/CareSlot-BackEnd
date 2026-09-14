package com.org.care_slot.service;

import com.org.care_slot.entity.Appointment;
import com.org.care_slot.entity.AppointmentSlot;
import com.org.care_slot.entity.Clinic;
import com.org.care_slot.entity.Doctor;
import com.org.care_slot.entity.PaymentTransaction;
import com.org.care_slot.enums.AppointmentEventType;
import com.org.care_slot.enums.AppointmentStatus;
import com.org.care_slot.enums.PaymentStatus;
import com.org.care_slot.enums.SlotStatus;
import com.org.care_slot.event.AppointmentEvent;
import com.org.care_slot.repository.AppointmentRepository;
import com.org.care_slot.repository.AppointmentSlotRepository;
import com.org.care_slot.repository.InvoiceRepository;
import com.org.care_slot.repository.PaymentTransactionRepository;
import com.org.care_slot.service.impl.VNPayServiceImpl;
import com.org.care_slot.util.VNPayUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VNPayConfirmationEventTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentSlotRepository appointmentSlotRepository;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private BookingLogService bookingLogService;

    @Mock
    private SlotAllocationService slotAllocationService;

    @Mock
    private PatientAppointmentMapper patientAppointmentMapper;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private VNPayServiceImpl vnPayService;

    private final String hashSecret = "TESTSECRET1234567890TESTSECRET12";
    private final String txnRef = "TXN-TEST-001";
    private Appointment appointment;
    private PaymentTransaction transaction;
    private AppointmentSlot slot;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(vnPayService, "hashSecret", hashSecret);
        ReflectionTestUtils.setField(vnPayService, "tmnCode", "TESTTMN");

        Clinic clinic = Clinic.builder()
                .id(1L)
                .name("CareSlot Central Clinic")
                .build();

        Doctor doctor = Doctor.builder()
                .id(1L)
                .fullName("Dr. Nguyen Van A")
                .clinic(clinic)
                .build();

        LocalDateTime now = SlotAllocationService.now();
        LocalDate appointmentDate = now.toLocalDate().plusDays(2);
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(10, 0);

        slot = new AppointmentSlot();
        slot.setId(100L);
        slot.setDoctor(doctor);
        slot.setAppointmentDate(appointmentDate);
        slot.setStartTime(startTime);
        slot.setEndTime(endTime);
        slot.setStatus(SlotStatus.HELD);
        slot.setHeldAt(now.minusMinutes(5));
        slot.setHoldExpiresAt(now.plusMinutes(10));

        appointment = new Appointment();
        appointment.setId(10L);
        appointment.setBookingCode("CS-20260907-010");
        appointment.setStatus(AppointmentStatus.PENDING_PAYMENT);
        appointment.setDepositAmount(BigDecimal.valueOf(100000));
        appointment.setSlot(slot);

        transaction = new PaymentTransaction();
        transaction.setTxnRef(txnRef);
        transaction.setStatus(PaymentStatus.PENDING);
        transaction.setAmount(BigDecimal.valueOf(100000));
        transaction.setAppointment(appointment);
    }

    private Map<String, String> buildParams(String responseCode, String transactionStatus) {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TmnCode", "TESTTMN");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_ResponseCode", responseCode);
        params.put("vnp_TransactionStatus", transactionStatus);
        params.put("vnp_Amount", "10000000"); // 100000 * 100
        params.put("vnp_PayDate", "20260914120000");
        params.put("vnp_TransactionNo", "12345678");
        params.put("vnp_BankCode", "NCB");

        String query = new TreeMap<>(params).entrySet().stream()
                .filter(e -> e.getKey().startsWith("vnp_") && !e.getKey().equals("vnp_SecureHash")
                        && !e.getKey().equals("vnp_SecureHashType") && e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.US_ASCII) + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.US_ASCII))
                .collect(Collectors.joining("&"));

        String secureHash = VNPayUtil.hmacSHA512(hashSecret, query);
        params.put("vnp_SecureHash", secureHash);
        return params;
    }

    private Map<String, String> buildParams(String responseCode) {
        return buildParams(responseCode, responseCode);
    }

    @Test
    @DisplayName("Scenario 1: Payment Success (00) on PENDING_PAYMENT -> transitions to CONFIRMED and publishes CONFIRMATION event")
    void testHandleCallback_PaymentSuccess_ShouldPublishEvent() {
        Map<String, String> params = buildParams("00", "00");

        when(paymentTransactionRepository.findByTxnRef(txnRef)).thenReturn(Optional.of(transaction));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        vnPayService.handleCallback(params);

        assertEquals(PaymentStatus.SUCCESS, transaction.getStatus());
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        assertEquals(SlotStatus.BOOKED, slot.getStatus());

        ArgumentCaptor<AppointmentEvent> eventCaptor = ArgumentCaptor.forClass(AppointmentEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        AppointmentEvent capturedEvent = eventCaptor.getValue();
        assertEquals(10L, capturedEvent.appointmentId());
        assertEquals(AppointmentEventType.CONFIRMATION, capturedEvent.eventType());
    }

    @Test
    @DisplayName("Scenario 2: Payment Failed (not 00) on PENDING_PAYMENT -> transaction FAILED, appointment not confirmed, does NOT publish event")
    void testHandleCallback_PaymentFailed_ShouldNotPublishEvent() {
        Map<String, String> params = buildParams("01", "01");

        when(paymentTransactionRepository.findByTxnRef(txnRef)).thenReturn(Optional.of(transaction));

        vnPayService.handleCallback(params);

        assertEquals(PaymentStatus.FAILED, transaction.getStatus());
        assertNotEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        assertEquals(AppointmentStatus.PENDING_PAYMENT, appointment.getStatus());

        verify(appointmentRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Scenario 3: Repeated Callback when already CONFIRMED -> Idempotency early return, does NOT publish event")
    void testHandleCallback_DuplicateRequest_ShouldNotPublishSecondEvent() {
        transaction.setStatus(PaymentStatus.SUCCESS);
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        Map<String, String> params = buildParams("00", "00");
        when(paymentTransactionRepository.findByTxnRef(txnRef)).thenReturn(Optional.of(transaction));

        vnPayService.handleCallback(params);

        verify(paymentTransactionRepository, never()).save(any());
        verify(appointmentRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Scenario 3b: Repeated IPN when already CONFIRMED -> Idempotency early return (rspCode 02), does NOT publish event")
    void testProcessIpn_DuplicateRequest_ShouldNotPublishSecondEvent() {
        transaction.setStatus(PaymentStatus.SUCCESS);
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        Map<String, String> params = buildParams("00", "00");
        when(paymentTransactionRepository.findByTxnRef(txnRef)).thenReturn(Optional.of(transaction));

        var ipnResponse = vnPayService.processIpn(params);

        assertEquals("02", ipnResponse.getRspCode());
        assertEquals("Order already confirmed", ipnResponse.getMessage());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Scenario 4: IPN Payment Success (00) on PENDING_PAYMENT -> transitions to CONFIRMED and publishes CONFIRMATION event")
    void testProcessIpn_PaymentSuccess_ShouldPublishEvent() {
        Map<String, String> params = buildParams("00", "00");

        when(paymentTransactionRepository.findByTxnRef(txnRef)).thenReturn(Optional.of(transaction));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        var ipnResponse = vnPayService.processIpn(params);

        assertEquals("00", ipnResponse.getRspCode());
        assertEquals("Confirm Success", ipnResponse.getMessage());
        assertEquals(PaymentStatus.SUCCESS, transaction.getStatus());
        assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
        assertEquals(SlotStatus.BOOKED, slot.getStatus());

        ArgumentCaptor<AppointmentEvent> eventCaptor = ArgumentCaptor.forClass(AppointmentEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        AppointmentEvent capturedEvent = eventCaptor.getValue();
        assertEquals(10L, capturedEvent.appointmentId());
        assertEquals(AppointmentEventType.CONFIRMATION, capturedEvent.eventType());
    }
}
