package com.org.care_slot.service;

import com.org.care_slot.dto.response.AppointmentSlotResponse;
import com.org.care_slot.enums.SlotStatus;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentSlotService {
    List<AppointmentSlotResponse> getDoctorSlots(Long doctorId, LocalDate date, LocalDate fromDate, LocalDate toDate, SlotStatus status);
}
