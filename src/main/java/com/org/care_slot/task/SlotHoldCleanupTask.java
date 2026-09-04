package com.org.care_slot.task;

import com.org.care_slot.entity.Appointment;
import com.org.care_slot.entity.AppointmentSlot;
import com.org.care_slot.enums.AppointmentStatus;
import com.org.care_slot.enums.SlotStatus;
import com.org.care_slot.repository.AppointmentRepository;
import com.org.care_slot.repository.AppointmentSlotRepository;
import com.org.care_slot.service.BookingLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlotHoldCleanupTask {

    private final AppointmentSlotRepository appointmentSlotRepository;
    private final AppointmentRepository appointmentRepository;
    private final BookingLogService bookingLogService;

    @Scheduled(fixedRate = 60000) // Chạy tự động mỗi 60 giây (1 phút)
    @Transactional
    public void runPeriodicSlotAndAppointmentMaintenance() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        // 1. Tự động giải phóng Slot HELD quá 10 phút & đổi Appointment PENDING_PAYMENT -> EXPIRED
        releaseExpiredHeldSlots(now);

        // 2. Tự động đổi Slot AVAILABLE quá giờ startTime -> OVER_DATE
        markOverDateAvailableSlots(today, currentTime);

        // 3. Tự động đổi Appointment CONFIRMED quá giờ startTime chưa check-in -> REJECTED (bệnh nhân không đến khám)
        markOverdueConfirmedAppointments(today, currentTime, now);
    }

    private void releaseExpiredHeldSlots(LocalDateTime now) {
        List<AppointmentSlot> expiredSlots = appointmentSlotRepository
                .findByStatusAndHoldExpiresAtBefore(SlotStatus.HELD, now);

        if (!expiredSlots.isEmpty()) {
            log.info("Found {} expired held slots to release.", expiredSlots.size());
            List<Appointment> expiredAppointments = new ArrayList<>();

            for (AppointmentSlot slot : expiredSlots) {
                slot.setStatus(SlotStatus.AVAILABLE);
                slot.setHeldAt(null);
                slot.setHoldExpiresAt(null);

                Optional<Appointment> appointmentOpt = appointmentRepository
                        .findBySlotIdAndStatus(slot.getId(), AppointmentStatus.PENDING_PAYMENT);

                if (appointmentOpt.isPresent()) {
                    Appointment appointment = appointmentOpt.get();
                    appointment.setStatus(AppointmentStatus.EXPIRED);
                    expiredAppointments.add(appointment);
                    bookingLogService.logEvent(appointment, "PENDING_PAYMENT", "EXPIRED", "HOLD_TIMEOUT", "Hết thời gian 10 phút giữ chỗ, lịch hẹn tự động chuyển sang EXPIRED", "SYSTEM");
                    log.info("Appointment ID {} status changed to EXPIRED due to hold timeout.", appointment.getId());
                }
            }

            appointmentSlotRepository.saveAll(expiredSlots);
            if (!expiredAppointments.isEmpty()) {
                appointmentRepository.saveAll(expiredAppointments);
            }
        }
    }

    private void markOverDateAvailableSlots(LocalDate today, LocalTime currentTime) {
        List<AppointmentSlot> overdueSlots = appointmentSlotRepository
                .findOverdueSlots(SlotStatus.AVAILABLE, today, currentTime);

        if (!overdueSlots.isEmpty()) {
            log.info("Found {} AVAILABLE slots past start time to mark as OVER_DATE.", overdueSlots.size());
            for (AppointmentSlot slot : overdueSlots) {
                slot.setStatus(SlotStatus.OVER_DATE);
            }
            appointmentSlotRepository.saveAll(overdueSlots);
        }
    }

    private void markOverdueConfirmedAppointments(LocalDate today, LocalTime currentTime, LocalDateTime now) {
        List<Appointment> overdueAppointments = appointmentRepository
                .findOverdueConfirmedAppointments(AppointmentStatus.CONFIRMED, today, currentTime);

        if (!overdueAppointments.isEmpty()) {
            log.info("Found {} CONFIRMED appointments past start time without check-in. Marking as REJECTED.", overdueAppointments.size());
            List<AppointmentSlot> slotsToUpdate = new ArrayList<>();

            for (Appointment appointment : overdueAppointments) {
                appointment.setStatus(AppointmentStatus.REJECTED);
                appointment.setRejectedAt(now);

                AppointmentSlot slot = appointment.getSlot();
                if (slot != null) {
                    slot.setStatus(SlotStatus.OVER_DATE);
                    slotsToUpdate.add(slot);
                }

                bookingLogService.logEvent(appointment, "CONFIRMED", "REJECTED", "APPOINTMENT_NO_SHOW", "Automatically marked REJECTED due to patient no-show past start time", "SYSTEM");
            }

            appointmentRepository.saveAll(overdueAppointments);
            if (!slotsToUpdate.isEmpty()) {
                appointmentSlotRepository.saveAll(slotsToUpdate);
            }
        }
    }
}
