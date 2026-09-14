package com.org.care_slot.service.listener;

import com.org.care_slot.enums.AppointmentEventType;
import com.org.care_slot.event.AppointmentEvent;
import com.org.care_slot.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AppointmentEventListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AppointmentEventListener appointmentEventListener;

    @Test
    @DisplayName("Should invoke sendAppointmentConfirmation when event type is CONFIRMATION")
    void testHandleAppointmentEvent_Confirmation() {
        Long appointmentId = 123L;
        AppointmentEvent event = new AppointmentEvent(appointmentId, AppointmentEventType.CONFIRMATION);

        appointmentEventListener.handleAppointmentEvent(event);

        verify(emailService, times(1)).sendAppointmentConfirmation(appointmentId);
        verify(emailService, never()).sendAppointmentCancellation(appointmentId);
        verify(emailService, never()).sendAppointmentReminder(appointmentId);
    }

    @Test
    @DisplayName("Should invoke sendAppointmentCancellation when event type is CANCELLATION")
    void testHandleAppointmentEvent_Cancellation() {
        Long appointmentId = 123L;
        AppointmentEvent event = new AppointmentEvent(appointmentId, AppointmentEventType.CANCELLATION);

        appointmentEventListener.handleAppointmentEvent(event);

        verify(emailService, times(1)).sendAppointmentCancellation(appointmentId);
        verify(emailService, never()).sendAppointmentConfirmation(appointmentId);
        verify(emailService, never()).sendAppointmentReminder(appointmentId);
    }

    @Test
    @DisplayName("Should invoke sendAppointmentReminder when event type is REMINDER")
    void testHandleAppointmentEvent_Reminder() {
        Long appointmentId = 123L;
        AppointmentEvent event = new AppointmentEvent(appointmentId, AppointmentEventType.REMINDER);

        appointmentEventListener.handleAppointmentEvent(event);

        verify(emailService, times(1)).sendAppointmentReminder(appointmentId);
        verify(emailService, never()).sendAppointmentConfirmation(appointmentId);
        verify(emailService, never()).sendAppointmentCancellation(appointmentId);
    }

    @Test
    @DisplayName("Should not send any email when event type is unrelated or event is null")
    void testHandleAppointmentEvent_UnrelatedEventOrNull() {
        AppointmentEvent rejectionEvent = new AppointmentEvent(123L, AppointmentEventType.REJECTION);
        AppointmentEvent rescheduleEvent = new AppointmentEvent(124L, AppointmentEventType.RESCHEDULE);
        appointmentEventListener.handleAppointmentEvent(rejectionEvent);
        appointmentEventListener.handleAppointmentEvent(rescheduleEvent);
        appointmentEventListener.handleAppointmentEvent(null);

        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Should handle and isolate exception thrown by EmailService on CONFIRMATION without propagating")
    void testHandleAppointmentEvent_Confirmation_ExceptionIsolation() {
        Long appointmentId = 456L;
        AppointmentEvent event = new AppointmentEvent(appointmentId, AppointmentEventType.CONFIRMATION);

        doThrow(new RuntimeException("Mail server down")).when(emailService).sendAppointmentConfirmation(appointmentId);

        assertDoesNotThrow(() -> appointmentEventListener.handleAppointmentEvent(event));
        verify(emailService, times(1)).sendAppointmentConfirmation(appointmentId);
    }

    @Test
    @DisplayName("Should handle and isolate exception thrown by EmailService on CANCELLATION without propagating")
    void testHandleAppointmentEvent_Cancellation_ExceptionIsolation() {
        Long appointmentId = 789L;
        AppointmentEvent event = new AppointmentEvent(appointmentId, AppointmentEventType.CANCELLATION);

        doThrow(new RuntimeException("Mail server down")).when(emailService).sendAppointmentCancellation(appointmentId);

        assertDoesNotThrow(() -> appointmentEventListener.handleAppointmentEvent(event));
        verify(emailService, times(1)).sendAppointmentCancellation(appointmentId);
    }

    @Test
    @DisplayName("Should handle and isolate exception thrown by EmailService on REMINDER without propagating")
    void testHandleAppointmentEvent_Reminder_ExceptionIsolation() {
        Long appointmentId = 999L;
        AppointmentEvent event = new AppointmentEvent(appointmentId, AppointmentEventType.REMINDER);

        doThrow(new RuntimeException("Mail server down")).when(emailService).sendAppointmentReminder(appointmentId);

        assertDoesNotThrow(() -> appointmentEventListener.handleAppointmentEvent(event));
        verify(emailService, times(1)).sendAppointmentReminder(appointmentId);
    }
}
