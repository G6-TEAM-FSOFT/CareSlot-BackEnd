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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
    }

    @Test
    @DisplayName("Should handle and isolate exception thrown by EmailService without propagating")
    void testHandleAppointmentEvent_ExceptionIsolation() {
        Long appointmentId = 456L;
        AppointmentEvent event = new AppointmentEvent(appointmentId, AppointmentEventType.CONFIRMATION);

        doThrow(new RuntimeException("Mail server down")).when(emailService).sendAppointmentConfirmation(appointmentId);

        assertDoesNotThrow(() -> appointmentEventListener.handleAppointmentEvent(event));
        verify(emailService, times(1)).sendAppointmentConfirmation(appointmentId);
    }
}
