package com.org.care_slot.task;

import com.org.care_slot.entity.Appointment;
import com.org.care_slot.entity.AppointmentSlot;
import com.org.care_slot.enums.AppointmentEventType;
import com.org.care_slot.enums.AppointmentStatus;
import com.org.care_slot.event.AppointmentEvent;
import com.org.care_slot.repository.AppointmentRepository;
import com.org.care_slot.service.SlotAllocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderTask {

    private final AppointmentRepository appointmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.reminder.window-minutes:10}")
    private int windowMinutes = 10;

    @Scheduled(fixedRateString = "${app.reminder.fixed-rate:60000}")
    @Transactional
    public void sendScheduledAppointmentReminders() {
        LocalDateTime now = SlotAllocationService.now();
        processRemindersAt(now);
    }

    @Transactional
    public void processRemindersAt(LocalDateTime now) {
        LocalDate targetDate = now.toLocalDate();
        LocalDate nextDate = now.plusDays(1).toLocalDate();

        List<Appointment> candidates = appointmentRepository.findCandidatesForReminder(targetDate, nextDate);
        if (candidates == null || candidates.isEmpty()) {
            return;
        }

        for (Appointment appointment : candidates) {
            processCandidate(appointment, now);
        }
    }

    private void processCandidate(Appointment appointment, LocalDateTime now) {
        if (appointment == null || appointment.getStatus() != AppointmentStatus.CONFIRMED
                || appointment.getReminderSentAt() != null) {
            return;
        }

        AppointmentSlot slot = appointment.getSlot();
        if (slot == null || slot.getAppointmentDate() == null || slot.getStartTime() == null) {
            return;
        }

        LocalDateTime scheduledStart = slot.getAppointmentDate().atTime(slot.getStartTime());
        LocalDateTime targetReminderTime = scheduledStart.minusHours(2);

        // If appointment was created or confirmed less than 2 hours before scheduled start, do not send a late reminder
        LocalDateTime confirmedAt = appointment.getApprovedAt() != null
                ? appointment.getApprovedAt()
                : appointment.getCreatedAt();
        if (confirmedAt != null && !confirmedAt.isBefore(targetReminderTime)) {
            log.debug("[REMINDER] Skip appointment {}: booked/confirmed at {} less than 2h before scheduled start {}",
                    appointment.getId(), confirmedAt, scheduledStart);
            return;
        }

        // Must be due: now >= targetReminderTime and within the acceptable window: now < targetReminderTime + windowMinutes
        if (now.isBefore(targetReminderTime) || !now.isBefore(targetReminderTime.plusMinutes(windowMinutes))) {
            return;
        }

        // Atomic update to avoid concurrent duplicate reminders
        int updated = appointmentRepository.markReminderSent(appointment.getId(), now);
        if (updated > 0) {
            appointment.setReminderSentAt(now);
            eventPublisher.publishEvent(new AppointmentEvent(appointment.getId(), AppointmentEventType.REMINDER));
            log.info("[REMINDER] Published REMINDER event for appointment id {}", appointment.getId());
        }
    }
}
