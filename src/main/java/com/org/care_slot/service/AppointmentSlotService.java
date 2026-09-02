package com.org.care_slot.service;

import com.org.care_slot.dto.request.SlotBatchCreateRequest;
import com.org.care_slot.dto.request.SlotCreateRequest;
import com.org.care_slot.dto.response.AppointmentSlotResponse;
import com.org.care_slot.dto.response.ExcelImportResultResponse;
import com.org.care_slot.enums.SlotStatus;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentSlotService {
    List<AppointmentSlotResponse> getDoctorSlots(Long doctorId, LocalDate date, LocalDate fromDate, LocalDate toDate, SlotStatus status);

    List<AppointmentSlotResponse> getClinicSlots(Long clinicId, Long doctorId, LocalDate date, LocalDate fromDate, LocalDate toDate, SlotStatus status, Long staffClinicId);
    AppointmentSlotResponse createSlot(Long clinicId, SlotCreateRequest request, Long staffClinicId);
    List<AppointmentSlotResponse> createBatchSlots(Long clinicId, SlotBatchCreateRequest request, Long staffClinicId);
    ExcelImportResultResponse importSlotsFromExcel(Long clinicId, MultipartFile file, Long staffClinicId);
}
