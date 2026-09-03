package com.org.care_slot.service;

import com.org.care_slot.dto.request.AppointmentCancelRequest;
import com.org.care_slot.dto.request.AppointmentCreateRequest;
import com.org.care_slot.dto.response.AppointmentResponse;
import com.org.care_slot.dto.response.PageResponse;
import com.org.care_slot.enums.AppointmentStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AppointmentService {
    AppointmentResponse createAppointment(Long userId, AppointmentCreateRequest request);
    PageResponse<AppointmentResponse> getUserAppointments(Long userId, AppointmentStatus status, Pageable pageable);
    AppointmentResponse getAppointmentDetail(Long id, Long userId);
    AppointmentResponse getAppointmentByBookingCode(String bookingCode, Long userId);
    AppointmentResponse cancelAppointment(Long id, Long userId, AppointmentCancelRequest request);

    PageResponse<AppointmentResponse> getClinicAppointments(Long clinicId, AppointmentStatus status, Long doctorId, LocalDate date, Pageable pageable, Long staffClinicId);
    AppointmentResponse getPartnerAppointmentDetail(Long clinicId, Long appointmentId, Long staffClinicId);
}
