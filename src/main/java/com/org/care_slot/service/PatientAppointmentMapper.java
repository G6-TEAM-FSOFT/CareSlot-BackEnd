package com.org.care_slot.service;

import com.org.care_slot.dto.response.PatientAppointmentResponse;
import com.org.care_slot.entity.*;
import com.org.care_slot.enums.AppointmentStatus;
import com.org.care_slot.enums.PaymentStatus;
import com.org.care_slot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PatientAppointmentMapper {
    private final VisitRepository visitRepository;
    private final EncounterRepository encounterRepository;
    private final PaymentTransactionRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public PatientAppointmentResponse toResponse(Appointment appointment) {
        AppointmentSlot slot = appointment.getSlot();
        Doctor doctor = slot.getDoctor();
        Clinic clinic = doctor.getClinic();
        Specialty specialty = doctor.getSpecialty();
        PatientProfile profile = appointment.getPatientProfile();
        Visit visit = appointment.getCheckedInAt() == null ? null
                : visitRepository.findByAppointmentId(appointment.getId()).orElse(null);
        boolean visible = visit != null && visit.getCheckedInAt() != null;
        Encounter initial = visible ? encounterRepository.findByVisitIdOrderByCreatedAtAsc(visit.getId()).stream()
                .filter(e -> "INITIAL_CONSULTATION".equals(e.getEncounterType())).findFirst().orElse(null) : null;

        PaymentTransaction transaction = paymentRepository
                .findFirstByAppointmentIdAndStatusOrderByCreatedAtDesc(appointment.getId(), PaymentStatus.SUCCESS)
                .orElseGet(() -> paymentRepository.findFirstByAppointmentIdOrderByCreatedAtDesc(appointment.getId()).orElse(null));
        Invoice invoice = invoiceRepository.findByAppointmentId(appointment.getId()).orElse(null);
        PatientAppointmentResponse.PaymentReceipt receipt = transaction != null
                ? PatientAppointmentResponse.PaymentReceipt.builder().status(transaction.getStatus().name())
                    .amount(transaction.getAmount()).provider(transaction.getPaymentProvider())
                    .transactionNo(transaction.getTransactionNo()).txnRef(transaction.getTxnRef())
                    .bankCode(transaction.getBankCode())
                    .paidAt(transaction.getStatus() == PaymentStatus.SUCCESS ? transaction.getPaymentTime() : null).build()
                : invoice == null ? null : PatientAppointmentResponse.PaymentReceipt.builder()
                    .status("PAID".equals(invoice.getStatus()) ? "SUCCESS" : invoice.getStatus())
                    .amount(invoice.getTotalAmount()).paidAt(invoice.getPaidAt()).build();
        boolean review = transaction != null && transaction.getStatus() == PaymentStatus.SUCCESS
                && appointment.getStatus() != AppointmentStatus.CONFIRMED
                && appointment.getStatus() != AppointmentStatus.CHECKED_IN
                && appointment.getStatus() != AppointmentStatus.COMPLETED;

        return PatientAppointmentResponse.builder()
                .id(appointment.getId()).bookingCode(appointment.getBookingCode())
                .patientProfileId(profile.getId()).patientName(profile.getFullName())
                .clinicId(clinic.getId()).clinicName(clinic.getName()).clinicAddress(clinic.getAddress())
                .specialtyId(specialty.getId()).specialtyName(specialty.getName())
                .appointmentDate(slot.getAppointmentDate()).startTime(slot.getStartTime()).endTime(slot.getEndTime())
                .symptomNote(appointment.getSymptomNote()).depositAmount(appointment.getDepositAmount())
                .status(appointment.getStatus()).createdAt(appointment.getCreatedAt())
                .holdExpiresAt(appointment.getStatus() == AppointmentStatus.PENDING_PAYMENT ? slot.getHoldExpiresAt() : null)
                .checkedInAt(appointment.getCheckedInAt()).assignmentVisible(visible)
                .visitId(visible ? visit.getId() : null)
                .doctorName(visible ? visit.getPrimaryDoctor().getFullName() : null)
                .roomName(initial != null ? initial.getRoom().getName() : null)
                .payment(receipt).paymentReviewRequired(review).build();
    }
}
