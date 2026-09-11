package com.org.care_slot.service.listener;

import com.org.care_slot.enums.AppointmentEventType;
import com.org.care_slot.event.AppointmentEvent;
import com.org.care_slot.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentEventListener {

    private final EmailService emailService;

    @Async("emailTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAppointmentEvent(AppointmentEvent event) {
        if (event == null || event.eventType() != AppointmentEventType.CONFIRMATION) {
            return;
        }

        log.info("[EVENT] Received AppointmentEvent: CONFIRMATION for appointment id {}", event.appointmentId());

        try {
            emailService.sendAppointmentConfirmation(event.appointmentId());
        } catch (Exception e) {
            log.error("[EVENT] Error processing confirmation email for appointment id {}: {}",
                    event.appointmentId(), e.getMessage(), e);
        }
    }
}
