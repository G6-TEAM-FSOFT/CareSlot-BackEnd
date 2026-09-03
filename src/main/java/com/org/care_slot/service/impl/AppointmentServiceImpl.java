package com.org.care_slot.service.impl;

import com.org.care_slot.dto.request.AppointmentCancelRequest;
import com.org.care_slot.dto.request.AppointmentCreateRequest;
import com.org.care_slot.dto.response.AppointmentResponse;
import com.org.care_slot.dto.response.PageResponse;
import com.org.care_slot.entity.Appointment;
import com.org.care_slot.entity.AppointmentSlot;
import com.org.care_slot.entity.PatientProfile;
import com.org.care_slot.enums.AppointmentStatus;
import com.org.care_slot.enums.SlotStatus;
import com.org.care_slot.exception.AppException;
import com.org.care_slot.exception.ErrorCode;
import com.org.care_slot.repository.AppointmentRepository;
import com.org.care_slot.repository.AppointmentSlotRepository;
import com.org.care_slot.repository.PatientProfileRepository;
import com.org.care_slot.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentSlotRepository appointmentSlotRepository;
    private final PatientProfileRepository patientProfileRepository;

    @org.springframework.beans.factory.annotation.Value("${vnpay.hold-timeout-minutes:10}")
    private long holdTimeoutMinutes;

    @Override
    public AppointmentResponse createAppointment(Long userId, AppointmentCreateRequest request) {
        PatientProfile patientProfile = patientProfileRepository
                .findByIdAndUserIdAndStatus(request.getPatientProfileId(), userId, "ACTIVE")
                .orElseThrow(() -> new AppException(ErrorCode.PATIENT_PROFILE_NOT_FOUND));

        AppointmentSlot slot = appointmentSlotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new AppException(ErrorCode.SLOT_NOT_FOUND));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new AppException(ErrorCode.SLOT_NOT_AVAILABLE);
        }

        // Tạm giữ slot theo số phút cấu hình trong yaml
        LocalDateTime now = LocalDateTime.now();
        slot.setStatus(SlotStatus.HELD);
        slot.setHeldAt(now);
        slot.setHoldExpiresAt(now.plusMinutes(holdTimeoutMinutes));
        appointmentSlotRepository.save(slot);

        BigDecimal consultationFee = slot.getDoctor() != null ? slot.getDoctor().getConsultationFee() : BigDecimal.ZERO;
        BigDecimal depositAmount = request.getDepositAmount() != null ? request.getDepositAmount() : BigDecimal.ZERO;

        String bookingCode = generateBookingCode();

        // Tạo Appointment ở trạng thái PENDING_PAYMENT để chờ thanh toán tiền cọc
        Appointment appointment = Appointment.builder()
                .bookingCode(bookingCode)
                .patientProfile(patientProfile)
                .slot(slot)
                .symptomNote(request.getSymptomNote())
                .consultationFee(consultationFee)
                .depositAmount(depositAmount)
                .status(AppointmentStatus.PENDING_PAYMENT)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> getUserAppointments(Long userId, AppointmentStatus status,
            Pageable pageable) {
        Page<Appointment> page = appointmentRepository.findByUserIdAndStatus(userId, status, pageable);
        List<AppointmentResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PageResponse.<AppointmentResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentDetail(Long id, Long userId) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        if (!appointment.getPatientProfile().getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return mapToResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentByBookingCode(String bookingCode, Long userId) {
        Appointment appointment = appointmentRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        if (!appointment.getPatientProfile().getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return mapToResponse(appointment);
    }

    @Override
    public AppointmentResponse cancelAppointment(Long id, Long userId, AppointmentCancelRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        if (!appointment.getPatientProfile().getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppException(ErrorCode.INVALID_APPOINTMENT_STATUS);
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(LocalDateTime.now());

        // Release slot to AVAILABLE
        AppointmentSlot slot = appointment.getSlot();
        if (slot != null) {
            slot.setStatus(SlotStatus.AVAILABLE);
            appointmentSlotRepository.save(slot);
        }

        Appointment updated = appointmentRepository.save(appointment);
        return mapToResponse(updated);
    }

    private String generateBookingCode() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String randomSuffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "CS" + datePrefix + randomSuffix;
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        AppointmentSlot slot = appointment.getSlot();
        PatientProfile profile = appointment.getPatientProfile();

        return AppointmentResponse.builder()
                .id(appointment.getId())
                .bookingCode(appointment.getBookingCode())
                .patientProfileId(profile != null ? profile.getId() : null)
                .patientName(profile != null ? profile.getFullName() : null)
                .doctorId(slot != null && slot.getDoctor() != null ? slot.getDoctor().getId() : null)
                .doctorName(slot != null && slot.getDoctor() != null ? slot.getDoctor().getFullName() : null)
                .clinicName(slot != null && slot.getDoctor() != null && slot.getDoctor().getClinic() != null
                        ? slot.getDoctor().getClinic().getName()
                        : null)
                .specialtyName(slot != null && slot.getDoctor() != null && slot.getDoctor().getSpecialty() != null
                        ? slot.getDoctor().getSpecialty().getName()
                        : null)
                .slotId(slot != null ? slot.getId() : null)
                .appointmentDate(slot != null ? slot.getAppointmentDate() : null)
                .startTime(slot != null ? slot.getStartTime() : null)
                .endTime(slot != null ? slot.getEndTime() : null)
                .roomName(slot != null ? slot.getRoomName() : null)
                .symptomNote(appointment.getSymptomNote())
                .consultationFee(appointment.getConsultationFee())
                .depositAmount(appointment.getDepositAmount())
                .status(appointment.getStatus())
                .createdAt(appointment.getCreatedAt())
                .approvedAt(appointment.getApprovedAt())
                .rejectedAt(appointment.getRejectedAt())
                .cancelledAt(appointment.getCancelledAt())
                .checkedInAt(appointment.getCheckedInAt())
                .build();
    }
}
