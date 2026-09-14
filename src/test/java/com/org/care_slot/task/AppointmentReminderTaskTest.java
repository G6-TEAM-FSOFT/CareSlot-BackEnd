package com.org.care_slot.task;

import com.org.care_slot.entity.Appointment;
import com.org.care_slot.entity.AppointmentSlot;
import com.org.care_slot.enums.AppointmentEventType;
import com.org.care_slot.enums.AppointmentStatus;
import com.org.care_slot.event.AppointmentEvent;
import com.org.care_slot.repository.AppointmentRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentReminderTaskTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AppointmentReminderTask reminderTask;

    private final LocalDateTime now = LocalDateTime.of(2026, 9, 15, 12, 0, 0);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reminderTask, "windowMinutes", 10);
    }

    private Appointment createAppointment(Long id, AppointmentStatus status, LocalDateTime startDateTime,
                                         LocalDateTime createdAt, LocalDateTime reminderSentAt) {
        AppointmentSlot slot = new AppointmentSlot();
        slot.setAppointmentDate(startDateTime.toLocalDate());
        slot.setStartTime(startDateTime.toLocalTime());
        slot.setEndTime(startDateTime.toLocalTime().plusMinutes(30));

        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setBookingCode("CS-20260915-" + id);
        appointment.setStatus(status);
        appointment.setSlot(slot);
        appointment.setCreatedAt(createdAt);
        appointment.setReminderSentAt(reminderSentAt);
        return appointment;
    }

    @Test
    @DisplayName("1. CONFIRMED appointment exactly around the 2-hour reminder point is selected")
    void testConfirmedAppointment_AtTwoHourMark_IsSelected() {
        // start is 14:00 (exactly 2 hours from now = 12:00)
        LocalDateTime start = LocalDateTime.of(2026, 9, 15, 14, 0, 0);
        LocalDateTime bookedAt = LocalDateTime.of(2026, 9, 15, 9, 0, 0);
        Appointment appointment = createAppointment(1L, AppointmentStatus.CONFIRMED, start, bookedAt, null);

        when(appointmentRepository.findCandidatesForReminder(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(appointment));
        when(appointmentRepository.markReminderSent(eq(1L), eq(now))).thenReturn(1);

        reminderTask.processRemindersAt(now);

        verify(appointmentRepository, times(1)).markReminderSent(1L, now);
        ArgumentCaptor<AppointmentEvent> eventCaptor = ArgumentCaptor.forClass(AppointmentEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        AppointmentEvent published = eventCaptor.getValue();
        assertNotNull(published);
        assertEquals(1L, published.appointmentId());
        assertEquals(AppointmentEventType.REMINDER, published.eventType());
    }

    @Test
    @DisplayName("2. Appointment not yet due is not selected")
    void testAppointment_NotYetDue_IsNotSelected() {
        // start is 14:30, 2-hour reminder time is 12:30. Current time is 12:00 -> not due yet
        LocalDateTime start = LocalDateTime.of(2026, 9, 15, 14, 30, 0);
        LocalDateTime bookedAt = LocalDateTime.of(2026, 9, 15, 9, 0, 0);
        Appointment appointment = createAppointment(2L, AppointmentStatus.CONFIRMED, start, bookedAt, null);

        when(appointmentRepository.findCandidatesForReminder(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(appointment));

        reminderTask.processRemindersAt(now);

        verify(appointmentRepository, never()).markReminderSent(any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("3. Appointment more than 2 hours away is not selected")
    void testAppointment_MoreThanTwoHoursAway_IsNotSelected() {
        // start is 16:00 (4 hours away)
        LocalDateTime start = LocalDateTime.of(2026, 9, 15, 16, 0, 0);
        LocalDateTime bookedAt = LocalDateTime.of(2026, 9, 15, 9, 0, 0);
        Appointment appointment = createAppointment(3L, AppointmentStatus.CONFIRMED, start, bookedAt, null);

        when(appointmentRepository.findCandidatesForReminder(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(appointment));

        reminderTask.processRemindersAt(now);

        verify(appointmentRepository, never()).markReminderSent(any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("4. CANCELLED appointment is not selected")
    void testCancelledAppointment_IsNotSelected() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 15, 14, 0, 0);
        LocalDateTime bookedAt = LocalDateTime.of(2026, 9, 15, 9, 0, 0);
        Appointment appointment = createAppointment(4L, AppointmentStatus.CANCELLED, start, bookedAt, null);

        when(appointmentRepository.findCandidatesForReminder(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(appointment));

        reminderTask.processRemindersAt(now);

        verify(appointmentRepository, never()).markReminderSent(any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("5. PENDING_PAYMENT appointment is not selected")
    void testPendingPaymentAppointment_IsNotSelected() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 15, 14, 0, 0);
        LocalDateTime bookedAt = LocalDateTime.of(2026, 9, 15, 9, 0, 0);
        Appointment appointment = createAppointment(5L, AppointmentStatus.PENDING_PAYMENT, start, bookedAt, null);

        when(appointmentRepository.findCandidatesForReminder(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(appointment));

        reminderTask.processRemindersAt(now);

        verify(appointmentRepository, never()).markReminderSent(any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("6. COMPLETED appointment is not selected")
    void testCompletedAppointment_IsNotSelected() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 15, 14, 0, 0);
        LocalDateTime bookedAt = LocalDateTime.of(2026, 9, 15, 9, 0, 0);
        Appointment appointment = createAppointment(6L, AppointmentStatus.COMPLETED, start, bookedAt, null);

        when(appointmentRepository.findCandidatesForReminder(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(appointment));

        reminderTask.processRemindersAt(now);

        verify(appointmentRepository, never()).markReminderSent(any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("7. Appointment with reminder already sent is not selected")
    void testAppointment_AlreadySent_IsNotSelected() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 15, 14, 0, 0);
        LocalDateTime bookedAt = LocalDateTime.of(2026, 9, 15, 9, 0, 0);
        LocalDateTime alreadySentAt = LocalDateTime.of(2026, 9, 15, 11, 58, 0);
        Appointment appointment = createAppointment(7L, AppointmentStatus.CONFIRMED, start, bookedAt, alreadySentAt);

        when(appointmentRepository.findCandidatesForReminder(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(appointment));

        reminderTask.processRemindersAt(now);

        verify(appointmentRepository, never()).markReminderSent(any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("8. Appointment created/confirmed less than 2 hours before start does not receive a late reminder")
    void testAppointment_BookedLessThanTwoHoursBeforeStart_IsNotSelected() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 15, 14, 0, 0);
        // Booked at 12:05 (less than 2 hours before 14:00 start)
        LocalDateTime lateBooking = LocalDateTime.of(2026, 9, 15, 12, 5, 0);
        Appointment appointment = createAppointment(8L, AppointmentStatus.CONFIRMED, start, lateBooking, null);

        LocalDateTime checkTime = LocalDateTime.of(2026, 9, 15, 12, 6, 0);
        when(appointmentRepository.findCandidatesForReminder(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(appointment));

        reminderTask.processRemindersAt(checkTime);

        verify(appointmentRepository, never()).markReminderSent(any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("9. The same appointment does not continuously produce reminder events after being marked as sent")
    void testAppointment_DoesNotContinuouslyProduceReminderEvents() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 15, 14, 0, 0);
        LocalDateTime bookedAt = LocalDateTime.of(2026, 9, 15, 9, 0, 0);
        Appointment appointment = createAppointment(9L, AppointmentStatus.CONFIRMED, start, bookedAt, null);

        when(appointmentRepository.findCandidatesForReminder(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(appointment));
        when(appointmentRepository.markReminderSent(eq(9L), eq(now))).thenReturn(1);

        // First run: appointment is claimed and marked as sent
        reminderTask.processRemindersAt(now);
        verify(eventPublisher, times(1)).publishEvent(any(AppointmentEvent.class));
        assertNotNull(appointment.getReminderSentAt());

        // Second run: appointment now has reminderSentAt != null
        reminderTask.processRemindersAt(now.plusMinutes(1));

        // Event publisher must still have been called only once
        verify(eventPublisher, times(1)).publishEvent(any(AppointmentEvent.class));
    }

    @Test
    @DisplayName("Concurrent execution: when markReminderSent returns 0, event is not published")
    void testConcurrentExecution_AlreadyClaimedByAnotherInstance_DoesNotPublishEvent() {
        LocalDateTime start = LocalDateTime.of(2026, 9, 15, 14, 0, 0);
        LocalDateTime bookedAt = LocalDateTime.of(2026, 9, 15, 9, 0, 0);
        Appointment appointment = createAppointment(10L, AppointmentStatus.CONFIRMED, start, bookedAt, null);

        when(appointmentRepository.findCandidatesForReminder(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(appointment));
        // Another instance claimed it first, so update returns 0 affected rows
        when(appointmentRepository.markReminderSent(eq(10L), eq(now))).thenReturn(0);

        reminderTask.processRemindersAt(now);

        verify(appointmentRepository, times(1)).markReminderSent(10L, now);
        verify(eventPublisher, never()).publishEvent(any());
    }
}
