package com.org.care_slot.task;

import com.org.care_slot.entity.Appointment;
import com.org.care_slot.entity.AppointmentSlot;
import com.org.care_slot.enums.AppointmentStatus;
import com.org.care_slot.enums.SlotStatus;
import com.org.care_slot.repository.AppointmentRepository;
import com.org.care_slot.repository.AppointmentSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlotHoldCleanupTask {

    private final AppointmentSlotRepository appointmentSlotRepository;
    private final AppointmentRepository appointmentRepository;

    @Scheduled(fixedRate = 60000) // Chạy tự động mỗi 60 giây (1 phút)
    @Transactional
    public void releaseExpiredHeldSlots() {
        LocalDateTime now = LocalDateTime.now();
        List<AppointmentSlot> expiredSlots = appointmentSlotRepository
                .findByStatusAndHoldExpiresAtBefore(SlotStatus.HELD, now);

        if (!expiredSlots.isEmpty()) {
            log.info("Found {} expired held slots to release.", expiredSlots.size());

            List<Appointment> expiredAppointments = new ArrayList<>();

            for (AppointmentSlot slot : expiredSlots) {
                // 1. Tự động chuyển Slot từ HELD về AVAILABLE
                slot.setStatus(SlotStatus.AVAILABLE);
                slot.setHeldAt(null);
                slot.setHoldExpiresAt(null);

                // 2. Chuyển Appointment PENDING_PAYMENT tương ứng sang EXPIRED
                Optional<Appointment> appointmentOpt = appointmentRepository
                        .findBySlotIdAndStatus(slot.getId(), AppointmentStatus.PENDING_PAYMENT);

                if (appointmentOpt.isPresent()) {
                    Appointment appointment = appointmentOpt.get();
                    appointment.setStatus(AppointmentStatus.EXPIRED);
                    expiredAppointments.add(appointment);
                    log.info("Appointment ID {} status changed to EXPIRED due to hold timeout.", appointment.getId());
                }
            }

            // Tối ưu hóa hiệu năng: Gom lại và lưu tất cả cùng một lượt (Batch Save)
            appointmentSlotRepository.saveAll(expiredSlots);
            if (!expiredAppointments.isEmpty()) {
                appointmentRepository.saveAll(expiredAppointments);
            }
        }
    }
}
