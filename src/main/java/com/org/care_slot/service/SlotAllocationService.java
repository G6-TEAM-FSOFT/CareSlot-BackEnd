package com.org.care_slot.service;

import com.org.care_slot.entity.*;
import com.org.care_slot.enums.SlotStatus;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SlotAllocationService {
    public static final ZoneId CLINIC_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final ClinicRepository clinicRepository;
    private final AppointmentSlotRepository slotRepository;
    private final AppointmentRepository appointmentRepository;
    private final BookingAllocationCursorRepository cursorRepository;

    public static LocalDateTime now() { return LocalDateTime.now(CLINIC_ZONE); }

    /** Every hold, release, payment confirmation and reassignment acquires this lock first. */
    @Transactional(propagation = Propagation.MANDATORY)
    public Clinic lockClinic(Long clinicId) {
        return clinicRepository.findByIdForAllocation(clinicId)
                .orElseThrow(() -> new AppException(ErrorCode.CLINIC_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<AppointmentSlot> findAvailableSlots(Long clinicId, Long specialtyId, LocalDate fromDate,
                                                   LocalDate toDate, Long excludedSlotId) {
        LocalDateTime current = now();
        List<AppointmentSlot> occupied = slotRepository.findOccupiedSlots(clinicId, fromDate, toDate);
        return slotRepository.findBookingCandidates(clinicId, specialtyId, fromDate, toDate).stream()
                .filter(this::hasValidResources)
                .filter(s -> s.getAppointmentDate().atTime(s.getStartTime()).isAfter(current))
                .filter(s -> !hasConflict(s, occupied, excludedSlotId))
                .toList();
    }

    /** No future-time restriction here: reception may replace an assigned slot at check-in. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void validateCandidate(AppointmentSlot slot, Long excludedSlotId) {
        if (slot.getStatus() != SlotStatus.AVAILABLE || !hasValidResources(slot)) {
            throw new AppException(ErrorCode.SLOT_NOT_AVAILABLE);
        }
        List<AppointmentSlot> occupied = slotRepository.findOccupiedSlots(slot.getDoctor().getClinic().getId(),
                slot.getAppointmentDate(), slot.getAppointmentDate());
        if (hasConflict(slot, occupied, excludedSlotId)) {
            throw new AppException(ErrorCode.SLOT_NOT_AVAILABLE);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public AppointmentSlot allocate(Long clinicId, Long specialtyId, LocalDate date,
                                     LocalTime startTime, LocalTime endTime) {
        List<AppointmentSlot> candidates = findAvailableSlots(clinicId, specialtyId, date, date, null).stream()
                .filter(s -> s.getStartTime().equals(startTime) && s.getEndTime().equals(endTime)).toList();
        if (candidates.isEmpty()) throw new AppException(ErrorCode.SLOT_NOT_AVAILABLE);

        Map<Long, Long> loads = new HashMap<>();
        for (Object[] row : appointmentRepository.countDailyDoctorLoad(clinicId, date, now())) {
            loads.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        long minimum = candidates.stream().mapToLong(s -> loads.getOrDefault(s.getDoctor().getId(), 0L))
                .min().orElseThrow();
        List<AppointmentSlot> leastLoaded = candidates.stream()
                .filter(s -> loads.getOrDefault(s.getDoctor().getId(), 0L) == minimum)
                .sorted(Comparator.comparing((AppointmentSlot s) -> s.getDoctor().getId())
                        .thenComparing(AppointmentSlot::getId)).toList();
        BookingAllocationCursor cursor = cursorRepository.findByClinicIdAndSpecialtyId(clinicId, specialtyId)
                .orElseGet(() -> BookingAllocationCursor.builder().clinicId(clinicId).specialtyId(specialtyId).build());
        long last = cursor.getLastDoctorId() == null ? -1L : cursor.getLastDoctorId();
        AppointmentSlot selected = leastLoaded.stream().filter(s -> s.getDoctor().getId() > last)
                .findFirst().orElse(leastLoaded.getFirst());
        cursor.setLastDoctorId(selected.getDoctor().getId());
        cursorRepository.save(cursor);
        return selected;
    }

    private boolean hasValidResources(AppointmentSlot slot) {
        Doctor doctor = slot.getDoctor();
        Room room = slot.getRoom();
        return doctor != null && "ACTIVE".equals(doctor.getStatus())
                && doctor.getClinic() != null && "ACTIVE".equals(doctor.getClinic().getStatus())
                && doctor.getSpecialty() != null && "ACTIVE".equals(doctor.getSpecialty().getStatus())
                && room != null && "ACTIVE".equals(room.getStatus()) && "CONSULTATION".equals(room.getRoomType())
                && room.getClinic() != null && Objects.equals(room.getClinic().getId(), doctor.getClinic().getId())
                && slot.getStartTime().isBefore(slot.getEndTime());
    }

    private boolean hasConflict(AppointmentSlot candidate, List<AppointmentSlot> occupied, Long excludedSlotId) {
        return occupied.stream().filter(s -> !Objects.equals(s.getId(), excludedSlotId))
                .filter(s -> !Objects.equals(s.getId(), candidate.getId()))
                .filter(s -> s.getAppointmentDate().equals(candidate.getAppointmentDate()))
                .filter(s -> s.getStartTime().isBefore(candidate.getEndTime())
                        && s.getEndTime().isAfter(candidate.getStartTime()))
                .anyMatch(s -> Objects.equals(s.getDoctor().getId(), candidate.getDoctor().getId())
                        || (s.getRoom() != null && Objects.equals(s.getRoom().getId(), candidate.getRoom().getId()))
                        // Legacy occupied rows can still protect a uniquely named room before normalization.
                        || (s.getRoom() == null && s.getRoomName() != null
                        && (s.getRoomName().equals(candidate.getRoom().getName())
                        || s.getRoomName().equals(candidate.getRoom().getRoomNumber()))));
    }
}
