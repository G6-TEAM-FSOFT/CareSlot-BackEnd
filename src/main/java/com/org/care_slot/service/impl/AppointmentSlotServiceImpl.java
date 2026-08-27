package com.org.care_slot.service.impl;

import com.org.care_slot.dto.response.AppointmentSlotResponse;
import com.org.care_slot.entity.AppointmentSlot;
import com.org.care_slot.enums.SlotStatus;
import com.org.care_slot.repository.AppointmentSlotRepository;
import com.org.care_slot.service.AppointmentSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentSlotServiceImpl implements AppointmentSlotService {

    private final AppointmentSlotRepository appointmentSlotRepository;

    @Override
    public List<AppointmentSlotResponse> getDoctorSlots(Long doctorId, LocalDate date, LocalDate fromDate, LocalDate toDate, SlotStatus status) {
        List<AppointmentSlot> slots = appointmentSlotRepository.findDoctorSlots(doctorId, date, fromDate, toDate, status);
        return slots.stream()
                .map(this::mapToSlotResponse)
                .toList();
    }

    private AppointmentSlotResponse mapToSlotResponse(AppointmentSlot slot) {
        return AppointmentSlotResponse.builder()
                .id(slot.getId())
                .doctorId(slot.getDoctor() != null ? slot.getDoctor().getId() : null)
                .doctorName(slot.getDoctor() != null ? slot.getDoctor().getFullName() : null)
                .appointmentDate(slot.getAppointmentDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .roomName(slot.getRoomName())
                .status(slot.getStatus())
                .build();
    }
}
