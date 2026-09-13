package com.org.care_slot.service;

import com.org.care_slot.dto.outpatient.CheckInRequest;
import com.org.care_slot.dto.response.AppointmentSlotResponse;
import com.org.care_slot.entity.*;
import com.org.care_slot.enums.AppointmentStatus;
import com.org.care_slot.enums.RoleType;
import com.org.care_slot.enums.SlotStatus;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceptionCheckInService {
    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotRepository slotRepository;
    private final UserRepository userRepository;
    private final VisitRepository visitRepository;
    private final EncounterRepository encounterRepository;
    private final InvoiceRepository invoiceRepository;
    private final SlotAllocationService allocationService;
    private final BookingLogService bookingLogService;

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public Long checkIn(CheckInRequest request, Long currentUserId) {
        User actor = requireStaff(currentUserId);
        // Acquire the same lock as booking before loading mutable appointment state.
        allocationService.lockClinic(actor.getClinic().getId());
        Appointment appointment = requireAppointment(request.getAppointmentId(), actor);
        Visit existing = visitRepository.findByAppointmentId(appointment.getId()).orElse(null);
        if (existing != null) return existing.getId();
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new AppException("Chỉ check-in lịch đã xác nhận và thanh toán tiền cọc.");
        }
        Invoice deposit = invoiceRepository.findByAppointmentId(appointment.getId())
                .orElseThrow(() -> new AppException("Chưa có hóa đơn đặt cọc đã thanh toán."));
        if (!"PAID".equals(deposit.getStatus()) || !"APPOINTMENT_DEPOSIT".equals(deposit.getInvoiceType())
                || deposit.getTotalAmount() == null || appointment.getDepositAmount() == null
                || deposit.getTotalAmount().compareTo(appointment.getDepositAmount()) != 0) {
            throw new AppException("Tiền cọc chưa được xác nhận thanh toán đầy đủ.");
        }

        AppointmentSlot original = appointment.getSlot();
        LocalDateTime now = SlotAllocationService.now();
        validateCheckInTime(original, now);
        AppointmentSlot assigned = original;
        if (request.getReplacementSlotId() != null && !request.getReplacementSlotId().equals(original.getId())) {
            String changeReason = (request.getReason() != null && !request.getReason().isBlank())
                    ? request.getReason().trim()
                    : "Lễ tân điều chuyển bác sĩ tại quầy tiếp đón";
            assigned = slotRepository.findById(request.getReplacementSlotId())
                    .orElseThrow(() -> new AppException("Slot thay thế không tồn tại."));
            if (!matchesReplacement(original, assigned)) {
                throw new AppException("Slot thay thế phải cùng cơ sở, chuyên khoa, ngày và khung giờ đã đặt.");
            }
            allocationService.validateCandidate(assigned, original.getId());
            assigned.setStatus(SlotStatus.BOOKED);
            assigned.setHeldAt(null);
            assigned.setHoldExpiresAt(null);
            slotRepository.save(assigned);
            original.setStatus(SlotStatus.OVER_DATE);
            original.setHeldAt(null);
            original.setHoldExpiresAt(null);
            slotRepository.save(original);
            appointment.setSlot(assigned);
            appointment.setConsultationFee(assigned.getDoctor().getConsultationFee());
            bookingLogService.logEvent(appointment, "CONFIRMED", "CONFIRMED", "ASSIGNMENT_CHANGED",
                    "Old " + describe(original) + "; new " + describe(assigned) + "; reason=" + changeReason,
                    "STAFF_" + actor.getId());
        }
        requireUsableAssignment(assigned);
        if (assigned.getStatus() != SlotStatus.BOOKED) {
            throw new AppException("Slot của lịch hẹn không còn ở trạng thái đã đặt.");
        }
        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        appointment.setCheckedInAt(now);
        appointmentRepository.save(appointment);
        Visit visit = Visit.builder()
                .visitCode("VIS-" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + appointment.getId())
                .appointment(appointment).patientProfile(appointment.getPatientProfile())
                .clinic(assigned.getDoctor().getClinic()).primaryDoctor(assigned.getDoctor())
                .status("ACTIVE").checkedInAt(now).build();
        visitRepository.save(visit);
        deposit.setVisit(visit);
        invoiceRepository.save(deposit);
        encounterRepository.save(Encounter.builder().visit(visit).encounterType("INITIAL_CONSULTATION")
                .room(assigned.getRoom()).doctor(assigned.getDoctor())
                .queueNumber("KHAM-" + appointment.getId()).status("WAITING").build());
        bookingLogService.logEvent(appointment, "CONFIRMED", "CHECKED_IN", "APPOINTMENT_CHECKED_IN",
                "Visit " + visit.getVisitCode() + "; final " + describe(assigned), "STAFF_" + actor.getId());
        return visit.getId();
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public Appointment reassignDoctor(Long appointmentId, Long replacementSlotId, String reason, Long currentUserId) {
        User actor = requireStaff(currentUserId);
        allocationService.lockClinic(actor.getClinic().getId());
        Appointment appointment = requireAppointment(appointmentId, actor);
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new AppException(ErrorCode.INVALID_APPOINTMENT_STATUS, "Chỉ có thể điều phối lại bác sĩ khi lịch hẹn ở trạng thái đã xác nhận (CONFIRMED).");
        }
        if (replacementSlotId == null) {
            throw new AppException(ErrorCode.SLOT_NOT_FOUND, "Vui lòng chọn slot bác sĩ thay thế.");
        }
        AppointmentSlot original = appointment.getSlot();
        if (replacementSlotId.equals(original.getId())) {
            throw new AppException(ErrorCode.SLOT_NOT_AVAILABLE, "Slot thay thế phải khác slot hiện tại.");
        }

        String changeReason = (reason != null && !reason.isBlank())
                ? reason.trim()
                : "Clinic Partner điều phối lại bác sĩ";

        AppointmentSlot assigned = slotRepository.findById(replacementSlotId)
                .orElseThrow(() -> new AppException(ErrorCode.SLOT_NOT_FOUND, "Slot thay thế không tồn tại."));
        if (!matchesReplacement(original, assigned)) {
            throw new AppException(ErrorCode.SLOT_NOT_AVAILABLE, "Slot thay thế phải cùng cơ sở, chuyên khoa, ngày và khung giờ đã đặt.");
        }
        allocationService.validateCandidate(assigned, original.getId());
        assigned.setStatus(SlotStatus.BOOKED);
        assigned.setHeldAt(null);
        assigned.setHoldExpiresAt(null);
        slotRepository.save(assigned);

        original.setStatus(SlotStatus.OVER_DATE);
        original.setHeldAt(null);
        original.setHoldExpiresAt(null);
        slotRepository.save(original);

        appointment.setSlot(assigned);
        appointment.setConsultationFee(assigned.getDoctor().getConsultationFee());
        appointmentRepository.save(appointment);

        String actorTag = actor.getRole() != null ? actor.getRole().name() + "_" + actor.getId() : "STAFF_" + actor.getId();
        bookingLogService.logEvent(appointment, "CONFIRMED", "CONFIRMED", "ASSIGNMENT_CHANGED",
                "Old " + describe(original) + "; new " + describe(assigned) + "; reason=" + changeReason,
                actorTag);

        return appointment;
    }

    @Transactional(readOnly = true)
    public List<AppointmentSlotResponse> getReplacementSlots(Long appointmentId, Long currentUserId) {
        User actor = requireStaff(currentUserId);
        Appointment appointment = requireAppointment(appointmentId, actor);
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new AppException(ErrorCode.INVALID_APPOINTMENT_STATUS, "Chỉ đổi phân công trước khi hoàn tất check-in (Lịch hẹn phải ở trạng thái CONFIRMED).");
        }
        AppointmentSlot original = appointment.getSlot();
        return slotRepository.findClinicSlots(actor.getClinic().getId(), null,
                        original.getAppointmentDate(), null, null, SlotStatus.AVAILABLE).stream()
                .filter(candidate -> matchesReplacement(original, candidate))
                .filter(candidate -> {
                    try {
                        allocationService.validateCandidate(candidate, original.getId());
                        return true;
                    } catch (AppException ex) {
                        return false;
                    }
                }).map(slot -> AppointmentSlotResponse.builder()
                        .id(slot.getId()).doctorId(slot.getDoctor().getId()).doctorName(slot.getDoctor().getFullName())
                        .appointmentDate(slot.getAppointmentDate()).startTime(slot.getStartTime()).endTime(slot.getEndTime())
                        .roomId(slot.getRoom().getId()).roomNumber(slot.getRoom().getRoomNumber())
                        .roomName(slot.getRoom().getName()).status(slot.getStatus()).build()).toList();
    }

    static void validateCheckInTime(AppointmentSlot slot, LocalDateTime now) {
        if (!now.toLocalDate().equals(slot.getAppointmentDate())
                || now.isBefore(slot.getAppointmentDate().atTime(slot.getStartTime()).minusHours(2))
                || !now.isBefore(slot.getAppointmentDate().atTime(slot.getEndTime()))) {
            throw new AppException("Check-in trong ngày khám, từ 2 giờ trước giờ hẹn đến hết khung giờ đã đặt.");
        }
    }

    private User requireStaff(Long currentUserId) {
        if (currentUserId == null) throw new AppException("Thiếu thông tin nhân viên tiếp đón.");
        User actor = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException("Nhân viên không tồn tại."));
        if (actor.getClinic() == null || !"ACTIVE".equals(actor.getStatus())
                || !(actor.getRole() == RoleType.RECEPTIONIST || actor.getRole() == RoleType.CLINIC_STAFF
                || actor.getRole() == RoleType.CLINIC_PARTNER || actor.getRole() == RoleType.CLINIC_ADMIN
                || actor.getRole() == RoleType.ADMIN)) {
            throw new AppException("Chỉ nhân viên tiếp đón thuộc cơ sở được thực hiện thao tác này.");
        }
        return actor;
    }

    private Appointment requireAppointment(Long appointmentId, User actor) {
        if (appointmentId == null) throw new AppException("Thiếu mã lịch hẹn.");
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException("Lịch hẹn không tồn tại."));
        if (appointment.getSlot() == null || appointment.getSlot().getDoctor() == null
                || !actor.getClinic().getId().equals(appointment.getSlot().getDoctor().getClinic().getId())) {
            throw new AppException("Lịch hẹn không thuộc cơ sở của nhân viên.");
        }
        return appointment;
    }

    static boolean matchesReplacement(AppointmentSlot original, AppointmentSlot candidate) {
        return !original.getId().equals(candidate.getId())
                && original.getDoctor().getClinic().getId().equals(candidate.getDoctor().getClinic().getId())
                && original.getDoctor().getSpecialty().getId().equals(candidate.getDoctor().getSpecialty().getId())
                && original.getAppointmentDate().equals(candidate.getAppointmentDate())
                && original.getStartTime().equals(candidate.getStartTime())
                && original.getEndTime().equals(candidate.getEndTime());
    }

    private void requireUsableAssignment(AppointmentSlot slot) {
        Room room = slot.getRoom();
        if (room == null || !"ACTIVE".equals(room.getStatus()) || !"CONSULTATION".equals(room.getRoomType())
                || !room.getClinic().getId().equals(slot.getDoctor().getClinic().getId())
                || !"ACTIVE".equals(slot.getDoctor().getStatus())) {
            throw new AppException("Bác sĩ/phòng hiện tại không hợp lệ. Vui lòng chọn slot thay thế có phòng khám đã cấu hình.");
        }
        boolean conflict = slotRepository.findClinicSlots(slot.getDoctor().getClinic().getId(), null,
                        slot.getAppointmentDate(), null, null, null).stream()
                .filter(other -> !other.getId().equals(slot.getId()))
                .filter(other -> other.getStatus() == SlotStatus.BOOKED || other.getStatus() == SlotStatus.HELD)
                .filter(other -> other.getStartTime().isBefore(slot.getEndTime()) && other.getEndTime().isAfter(slot.getStartTime()))
                .anyMatch(other -> other.getDoctor().getId().equals(slot.getDoctor().getId())
                        || (other.getRoom() != null && other.getRoom().getId().equals(room.getId())));
        if (conflict) throw new AppException("Bác sĩ hoặc phòng có lịch trùng. Vui lòng chọn phân công khác.");
    }

    private String describe(AppointmentSlot slot) {
        return "slot=" + slot.getId() + ",doctor=" + slot.getDoctor().getId()
                + ",room=" + (slot.getRoom() == null ? "unresolved" : slot.getRoom().getId());
    }
}
