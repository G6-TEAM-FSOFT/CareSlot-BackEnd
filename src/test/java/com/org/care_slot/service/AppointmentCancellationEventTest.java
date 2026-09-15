package com.org.care_slot.service;

import com.org.care_slot.dto.request.AppointmentCancelRequest;
import com.org.care_slot.dto.response.AppointmentResponse;
import com.org.care_slot.entity.Appointment;
import com.org.care_slot.entity.AppointmentSlot;
import com.org.care_slot.entity.Doctor;
import com.org.care_slot.entity.PatientProfile;
import com.org.care_slot.entity.Specialty;
import com.org.care_slot.entity.User;
import com.org.care_slot.enums.AppointmentEventType;
import com.org.care_slot.enums.AppointmentStatus;
import com.org.care_slot.enums.SlotStatus;
import com.org.care_slot.event.AppointmentEvent;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.repository.AppointmentRepository;
import com.org.care_slot.repository.AppointmentSlotRepository;
import com.org.care_slot.repository.InvoiceRepository;
import com.org.care_slot.repository.PatientProfileRepository;
import com.org.care_slot.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentCancellationEventTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentSlotRepository appointmentSlotRepository;

    @Mock
    private PatientProfileRepository patientProfileRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private BookingLogService bookingLogService;

    @Mock
    private SlotAllocationService slotAllocationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Appointment sampleAppointment;
    private AppointmentSlot sampleSlot;
    private final Long appointmentId = 1L;
    private final Long userId = 10L;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(userId);
        user.setEmail("patient@example.com");

        PatientProfile patientProfile = new PatientProfile();
        patientProfile.setId(20L);
        patientProfile.setUser(user);
        patientProfile.setFullName("Nguyen Van A");

        Specialty specialty = new Specialty();
        specialty.setId(1L);
        specialty.setName("Noi khoa");

        Doctor doctor = new Doctor();
        doctor.setId(5L);
        doctor.setFullName("Dr. Tran B");
        doctor.setSpecialty(specialty);

        sampleSlot = new AppointmentSlot();
        sampleSlot.setId(100L);
        sampleSlot.setDoctor(doctor);
        sampleSlot.setAppointmentDate(LocalDate.now().plusDays(2));
        sampleSlot.setStartTime(LocalTime.of(9, 0));
        sampleSlot.setEndTime(LocalTime.of(9, 30));
        sampleSlot.setStatus(SlotStatus.BOOKED);

        sampleAppointment = new Appointment();
        sampleAppointment.setId(appointmentId);
        sampleAppointment.setBookingCode("CS-20260907-001");
        sampleAppointment.setPatientProfile(patientProfile);
        sampleAppointment.setSlot(sampleSlot);
        sampleAppointment.setStatus(AppointmentStatus.CONFIRMED);
        sampleAppointment.setDepositAmount(BigDecimal.valueOf(100000));
        sampleAppointment.setConsultationFee(BigDecimal.valueOf(300000));
    }

    @Test
    @DisplayName("Should publish exactly one CANCELLATION event when cancellation succeeds")
    void testCancelAppointment_Success_ShouldPublishCancellationEvent() {
        AppointmentCancelRequest request = new AppointmentCancelRequest("Co viec ban dot xuat");

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(sampleAppointment));
        when(appointmentSlotRepository.save(any(AppointmentSlot.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentResponse response = appointmentService.cancelAppointment(appointmentId, userId, request);

        assertNotNull(response);
        assertEquals(AppointmentStatus.CANCELLED, response.getStatus());

        ArgumentCaptor<AppointmentEvent> eventCaptor = ArgumentCaptor.forClass(AppointmentEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        AppointmentEvent publishedEvent = eventCaptor.getValue();
        assertNotNull(publishedEvent);
        assertEquals(appointmentId, publishedEvent.appointmentId());
        assertEquals(AppointmentEventType.CANCELLATION, publishedEvent.eventType());

        verify(bookingLogService, times(1)).logEvent(
                any(Appointment.class),
                eq("CONFIRMED"),
                eq("CANCELLED"),
                eq("APPOINTMENT_CANCELLED"),
                eq("Co viec ban dot xuat"),
                eq("PATIENT")
        );
    }

    @Test
    @DisplayName("Should not publish event when appointment is not found")
    void testCancelAppointment_NotFound_ShouldNotPublishEvent() {
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () ->
                appointmentService.cancelAppointment(appointmentId, userId, new AppointmentCancelRequest("test"))
        );
        assertEquals(ErrorCode.APPOINTMENT_NOT_FOUND, exception.getErrorCode());

        verify(eventPublisher, never()).publishEvent(any());
        verify(bookingLogService, never()).logEvent(any(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should not publish event when user is unauthorized")
    void testCancelAppointment_UnauthorizedUser_ShouldNotPublishEvent() {
        Long unauthorizedUserId = 999L;
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(sampleAppointment));

        AppException exception = assertThrows(AppException.class, () ->
                appointmentService.cancelAppointment(appointmentId, unauthorizedUserId, new AppointmentCancelRequest("test"))
        );
        assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());

        verify(eventPublisher, never()).publishEvent(any());
        verify(bookingLogService, never()).logEvent(any(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should not publish event when appointment status is not eligible for cancellation")
    void testCancelAppointment_InvalidStatus_ShouldNotPublishEvent() {
        sampleAppointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(sampleAppointment));

        AppException exception = assertThrows(AppException.class, () ->
                appointmentService.cancelAppointment(appointmentId, userId, new AppointmentCancelRequest("test"))
        );
        assertEquals(ErrorCode.INVALID_APPOINTMENT_STATUS, exception.getErrorCode());

        verify(eventPublisher, never()).publishEvent(any());
        verify(bookingLogService, never()).logEvent(any(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should not publish event when cancellation happens past appointment start time")
    void testCancelAppointment_PastStartTime_ShouldNotPublishEvent() {
        // Slot is in the past
        sampleSlot.setAppointmentDate(LocalDate.now().minusDays(1));
        sampleSlot.setStartTime(LocalTime.of(8, 0));

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(sampleAppointment));
        when(appointmentSlotRepository.save(any(AppointmentSlot.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppException exception = assertThrows(AppException.class, () ->
                appointmentService.cancelAppointment(appointmentId, userId, new AppointmentCancelRequest("test"))
        );
        assertEquals(ErrorCode.CANNOT_CANCEL_PAST_START_TIME, exception.getErrorCode());

        verify(eventPublisher, never()).publishEvent(any());
    }
}
