package com.org.care_slot.task;

import com.org.care_slot.entity.Appointment;
import com.org.care_slot.entity.AppointmentSlot;
import com.org.care_slot.enums.AppointmentStatus;
import com.org.care_slot.enums.SlotStatus;
import com.org.care_slot.repository.AppointmentRepository;
import com.org.care_slot.repository.AppointmentSlotRepository;
import com.org.care_slot.service.BookingLogService;
import com.org.care_slot.service.SlotAllocationService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeSet;

@Component
@RequiredArgsConstructor
public class SlotHoldCleanupTask {
    private final AppointmentSlotRepository appointmentSlotRepository;
    private final AppointmentRepository appointmentRepository;
    private final BookingLogService bookingLogService;
    private final SlotAllocationService slotAllocationService;
    private final EntityManager entityManager;

    @Scheduled(fixedRate = 60000)
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void runPeriodicSlotAndAppointmentMaintenance() {
        LocalDateTime now = SlotAllocationService.now();
        List<AppointmentSlot> expired = appointmentSlotRepository
                .findByStatusAndHoldExpiresAtBefore(SlotStatus.HELD, now);
        List<AppointmentSlot> past = appointmentSlotRepository
                .findOverdueSlots(SlotStatus.AVAILABLE, now.toLocalDate(), now.toLocalTime());
        List<Appointment> noShows = appointmentRepository
                .findOverdueConfirmedAppointments(AppointmentStatus.CONFIRMED, now.toLocalDate(), now.toLocalTime());

        // Deterministic lock order, shared with allocation, payment, cancellation and check-in.
        TreeSet<Long> clinics = new TreeSet<>();
        expired.forEach(s -> clinics.add(s.getDoctor().getClinic().getId()));
        past.forEach(s -> clinics.add(s.getDoctor().getClinic().getId()));
        noShows.forEach(a -> clinics.add(a.getSlot().getDoctor().getClinic().getId()));
        clinics.forEach(slotAllocationService::lockClinic);

        for (AppointmentSlot slot : expired) {
            entityManager.refresh(slot);
            if (slot.getStatus() != SlotStatus.HELD || slot.getHoldExpiresAt() == null
                    || slot.getHoldExpiresAt().isAfter(now)) continue;
            appointmentRepository.findBySlotIdAndStatus(slot.getId(), AppointmentStatus.PENDING_PAYMENT)
                    .ifPresent(appointment -> {
                        entityManager.refresh(appointment);
                        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) return;
                        appointment.setStatus(AppointmentStatus.EXPIRED);
                        appointmentRepository.save(appointment);
                        bookingLogService.logEvent(appointment, "PENDING_PAYMENT", "EXPIRED", "HOLD_TIMEOUT",
                                "Hết thời gian giữ chỗ; lịch hẹn đã hết hạn.", "SYSTEM");
                    });
            release(slot, now);
        }
        for (AppointmentSlot slot : past) {
            entityManager.refresh(slot);
            if (slot.getStatus() == SlotStatus.AVAILABLE
                    && !now.isBefore(slot.getAppointmentDate().atTime(slot.getStartTime()))) {
                slot.setStatus(SlotStatus.OVER_DATE);
                appointmentSlotRepository.save(slot);
            }
        }
        for (Appointment appointment : noShows) {
            entityManager.refresh(appointment);
            AppointmentSlot slot = appointment.getSlot();
            entityManager.refresh(slot);
            // Check-in remains possible during the booked interval.
            if (appointment.getStatus() != AppointmentStatus.CONFIRMED
                    || now.isBefore(slot.getAppointmentDate().atTime(slot.getEndTime()))) continue;
            appointment.setStatus(AppointmentStatus.REJECTED);
            appointment.setRejectedAt(now);
            appointmentRepository.save(appointment);
            if (slot.getStatus() == SlotStatus.BOOKED) {
                slot.setStatus(SlotStatus.OVER_DATE);
                appointmentSlotRepository.save(slot);
            }
            bookingLogService.logEvent(appointment, "CONFIRMED", "REJECTED", "APPOINTMENT_NO_SHOW",
                    "Đã hết khung giờ khám nhưng bệnh nhân chưa check-in.", "SYSTEM");
        }
    }

    private void release(AppointmentSlot slot, LocalDateTime now) {
        slot.setStatus(now.isBefore(slot.getAppointmentDate().atTime(slot.getStartTime()))
                ? SlotStatus.AVAILABLE : SlotStatus.OVER_DATE);
        slot.setHeldAt(null);
        slot.setHoldExpiresAt(null);
        appointmentSlotRepository.save(slot);
    }
}
