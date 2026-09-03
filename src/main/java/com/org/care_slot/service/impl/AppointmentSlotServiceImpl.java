package com.org.care_slot.service.impl;

import com.org.care_slot.dto.request.SlotBatchCreateRequest;
import com.org.care_slot.dto.request.SlotCreateRequest;
import com.org.care_slot.dto.response.AppointmentSlotResponse;
import com.org.care_slot.dto.response.ExcelImportResultResponse;
import com.org.care_slot.entity.AppointmentSlot;
import com.org.care_slot.entity.Doctor;
import com.org.care_slot.enums.SlotStatus;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.repository.AppointmentSlotRepository;
import com.org.care_slot.repository.DoctorRepository;
import com.org.care_slot.service.AppointmentSlotService;
import com.org.care_slot.util.ExcelHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentSlotServiceImpl implements AppointmentSlotService {

    private final AppointmentSlotRepository appointmentSlotRepository;
    private final DoctorRepository doctorRepository;

    @Override
    public List<AppointmentSlotResponse> getDoctorSlots(Long doctorId, LocalDate date, LocalDate fromDate, LocalDate toDate, SlotStatus status) {
        List<AppointmentSlot> slots = appointmentSlotRepository.findDoctorSlots(doctorId, date, fromDate, toDate, status);
        return slots.stream()
                .map(this::mapToSlotResponse)
                .toList();
    }

    @Override
    public List<AppointmentSlotResponse> getClinicSlots(Long clinicId, Long doctorId, LocalDate date, LocalDate fromDate, LocalDate toDate, SlotStatus status, Long staffClinicId) {
        if (staffClinicId == null || !staffClinicId.equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        List<AppointmentSlot> slots = appointmentSlotRepository.findClinicSlots(clinicId, doctorId, date, fromDate, toDate, status);
        return slots.stream()
                .map(this::mapToSlotResponse)
                .toList();
    }

    @Override
    @Transactional
    public AppointmentSlotResponse createSlot(Long clinicId, SlotCreateRequest request, Long staffClinicId) {
        if (staffClinicId == null || !staffClinicId.equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        AppointmentSlot slot = processSingleSlotCreation(clinicId, request);
        return mapToSlotResponse(slot);
    }

    @Override
    @Transactional
    public List<AppointmentSlotResponse> createBatchSlots(Long clinicId, SlotBatchCreateRequest request, Long staffClinicId) {
        if (staffClinicId == null || !staffClinicId.equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        List<AppointmentSlotResponse> responses = new ArrayList<>();
        if (request.getSlots() != null) {
            for (SlotCreateRequest slotRequest : request.getSlots()) {
                AppointmentSlot slot = processSingleSlotCreation(clinicId, slotRequest);
                responses.add(mapToSlotResponse(slot));
            }
        }
        return responses;
    }

    @Override
    @Transactional
    public ExcelImportResultResponse importSlotsFromExcel(Long clinicId, MultipartFile file, Long staffClinicId) {
        if (staffClinicId == null || !staffClinicId.equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        if (!ExcelHelper.hasExcelFormat(file)) {
            throw new AppException(ErrorCode.INVALID_EXCEL_FILE);
        }

        List<SlotCreateRequest> rawRequests;
        try {
            rawRequests = ExcelHelper.excelToSlotRequests(file.getInputStream());
        } catch (IOException e) {
            throw new AppException(ErrorCode.INVALID_EXCEL_FILE);
        }

        ExcelImportResultResponse result = ExcelImportResultResponse.builder()
                .totalRows(rawRequests.size())
                .build();

        int rowIdx = 2; // Row index in Excel (Row 1 is header)
        for (SlotCreateRequest req : rawRequests) {
            try {
                AppointmentSlot slot = processSingleSlotCreation(clinicId, req);
                result.getImportedSlots().add(mapToSlotResponse(slot));
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (AppException ae) {
                result.setFailedCount(result.getFailedCount() + 1);
                result.getErrors().add("Row " + rowIdx + " (Doctor ID " + req.getDoctorId() + "): " + ae.getMessage());
            } catch (Exception e) {
                result.setFailedCount(result.getFailedCount() + 1);
                result.getErrors().add("Row " + rowIdx + " (Doctor ID " + req.getDoctorId() + "): " + e.getMessage());
            }
            rowIdx++;
        }

        return result;
    }

    private AppointmentSlot processSingleSlotCreation(Long clinicId, SlotCreateRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null || !request.getStartTime().isBefore(request.getEndTime())) {
            throw new AppException(ErrorCode.INVALID_SLOT_TIME);
        }

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));

        if (!doctor.getClinic().getId().equals(clinicId)) {
            throw new AppException(ErrorCode.FORBIDDEN_CLINIC_ACCESS);
        }

        boolean overlap = appointmentSlotRepository.existsOverlappingSlot(
                request.getDoctorId(),
                request.getAppointmentDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        if (overlap) {
            throw new AppException(ErrorCode.SLOT_TIME_OVERLAP);
        }

        AppointmentSlot slot = AppointmentSlot.builder()
                .doctor(doctor)
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .roomName(request.getRoomName())
                .status(SlotStatus.AVAILABLE)
                .build();

        return appointmentSlotRepository.save(slot);
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
