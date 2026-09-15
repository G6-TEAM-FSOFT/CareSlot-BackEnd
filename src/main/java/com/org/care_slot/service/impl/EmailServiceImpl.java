package com.org.care_slot.service.impl;

import com.org.care_slot.entity.Appointment;
import com.org.care_slot.entity.AppointmentSlot;
import com.org.care_slot.entity.Clinic;
import com.org.care_slot.entity.Doctor;
import com.org.care_slot.entity.PatientProfile;
import com.org.care_slot.entity.BookingLog;
import com.org.care_slot.repository.AppointmentRepository;
import com.org.care_slot.repository.BookingLogRepository;
import com.org.care_slot.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final AppointmentRepository appointmentRepository;
    private final BookingLogRepository bookingLogRepository;

    @Value("${spring.mail.username:no-reply@careslot.com}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    @Override
    @Transactional(readOnly = true)
    public void sendAppointmentConfirmation(Long appointmentId) {
        if (appointmentId == null) {
            log.warn("[EMAIL] Skip confirmation email: appointmentId is null");
            return;
        }

        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            log.warn("[EMAIL] Skip confirmation email: Appointment not found with id {}", appointmentId);
            return;
        }

        String recipientEmail = getRecipientEmail(appointment);
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("[EMAIL] Skip confirmation email: No recipient email found for booking {}",
                    appointment.getBookingCode());
            return;
        }

        try {
            Context context = buildBaseContext(appointment);
            String htmlContent = templateEngine.process("email/appointment-confirmation", context);

            sendHtmlEmail(
                    recipientEmail,
                    "[" + appointment.getBookingCode() + "] Xác nhận đặt lịch khám thành công - CareSlot",
                    htmlContent);
            log.info("[EMAIL] Successfully sent confirmation email to {} for booking {}", recipientEmail,
                    appointment.getBookingCode());
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send confirmation email for booking {}: {}", appointment.getBookingCode(),
                    e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void sendAppointmentCancellation(Long appointmentId) {
        if (appointmentId == null) {
            log.warn("[EMAIL] Skip cancellation email: appointmentId is null");
            return;
        }

        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            log.warn("[EMAIL] Skip cancellation email: Appointment not found with id {}", appointmentId);
            return;
        }

        String recipientEmail = getRecipientEmail(appointment);
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("[EMAIL] Skip cancellation email: No recipient email found for booking {}",
                    appointment.getBookingCode());
            return;
        }

        try {
            Context context = buildBaseContext(appointment);

            String cancelledAtStr = appointment.getCancelledAt() != null
                    ? appointment.getCancelledAt().format(DATE_TIME_FORMATTER)
                    : "N/A";
            context.setVariable("cancelledAt", cancelledAtStr);

            String reason = resolveCancellationReason(appointmentId);
            context.setVariable("cancellationReason", reason);

            String htmlContent = templateEngine.process("email/appointment-cancellation", context);

            sendHtmlEmail(
                    recipientEmail,
                    "[" + appointment.getBookingCode() + "] Thông báo hủy lịch khám thành công - CareSlot",
                    htmlContent);
            log.info("[EMAIL] Successfully sent cancellation email to {} for booking {}", recipientEmail,
                    appointment.getBookingCode());
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send cancellation email for booking {}: {}", appointment.getBookingCode(),
                    e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void sendAppointmentReminder(Long appointmentId) {
        if (appointmentId == null) {
            log.warn("[EMAIL] Skip reminder email: appointmentId is null");
            return;
        }

        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            log.warn("[EMAIL] Skip reminder email: Appointment not found with id {}", appointmentId);
            return;
        }

        String recipientEmail = getRecipientEmail(appointment);
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("[EMAIL] Skip reminder email: No recipient email found for booking {}",
                    appointment.getBookingCode());
            return;
        }

        try {
            Context context = buildBaseContext(appointment);
            String htmlContent = templateEngine.process("email/appointment-reminder", context);

            sendHtmlEmail(
                    recipientEmail,
                    "[" + appointment.getBookingCode() + "] Nhắc nhở lịch khám sắp diễn ra - CareSlot",
                    htmlContent);
            log.info("[EMAIL] Successfully sent reminder email to {} for booking {}", recipientEmail,
                    appointment.getBookingCode());
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send reminder email for booking {}: {}", appointment.getBookingCode(),
                    e.getMessage(), e);
            try {
                appointmentRepository.resetReminderSent(appointmentId);
                log.info("[EMAIL] Reset reminderSentAt for appointment id {} to allow scheduler retry", appointmentId);
            } catch (Exception resetEx) {
                log.error("[EMAIL] Failed to reset reminderSentAt for appointment id {}: {}", appointmentId,
                        resetEx.getMessage(), resetEx);
            }
        }
    }

    private String resolveCancellationReason(Long appointmentId) {
        try {
            List<BookingLog> logs = bookingLogRepository.findByAppointmentIdOrderByCreatedAtAsc(appointmentId);
            if (logs != null && !logs.isEmpty()) {
                for (int i = logs.size() - 1; i >= 0; i--) {
                    BookingLog log = logs.get(i);
                    if ("APPOINTMENT_CANCELLED".equals(log.getEventType())
                            && log.getNote() != null && !log.getNote().isBlank()) {
                        return log.getNote();
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("[EMAIL] Could not retrieve cancellation reason for appointment {}: {}", appointmentId, ex.getMessage());
        }
        return "Hủy theo yêu cầu của quý khách";
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }

    private String getRecipientEmail(Appointment appointment) {
        PatientProfile patient = appointment.getPatientProfile();
        if (patient != null && patient.getUser() != null) {
            return patient.getUser().getEmail();
        }
        return null;
    }

    private Context buildBaseContext(Appointment appointment) {
        Context context = new Context();
        PatientProfile patient = appointment.getPatientProfile();
        AppointmentSlot slot = appointment.getSlot();
        Doctor doctor = slot != null ? slot.getDoctor() : null;
        Clinic clinic = doctor != null ? doctor.getClinic() : null;

        context.setVariable("bookingCode", appointment.getBookingCode());
        context.setVariable("patientName", patient != null ? patient.getFullName() : "Quý khách");
        context.setVariable("patientPhone", patient != null ? patient.getPhone() : "N/A");
        context.setVariable("doctorName", doctor != null ? doctor.getFullName() : "N/A");
        context.setVariable("doctorTitle", doctor != null && doctor.getTitle() != null ? doctor.getTitle() : "Bác sĩ");
        context.setVariable("specialtyName",
                doctor != null && doctor.getSpecialty() != null ? doctor.getSpecialty().getName() : "Đa khoa");
        context.setVariable("clinicName", clinic != null ? clinic.getName() : "CareSlot Clinic");
        context.setVariable("clinicAddress", clinic != null ? clinic.getAddress() : "N/A");
        context.setVariable("clinicPhone",
                clinic != null && clinic.getPhone() != null ? clinic.getPhone() : "1900 xxxx");

        if (slot != null) {
            context.setVariable("appointmentDate",
                    slot.getAppointmentDate() != null ? slot.getAppointmentDate().format(DATE_FORMATTER) : "N/A");
            context.setVariable("startTime",
                    slot.getStartTime() != null ? slot.getStartTime().format(TIME_FORMATTER) : "N/A");
            context.setVariable("endTime",
                    slot.getEndTime() != null ? slot.getEndTime().format(TIME_FORMATTER) : "N/A");
            context.setVariable("roomName", slot.getRoomName() != null ? slot.getRoomName() : "Đang cập nhật");
        }

        context.setVariable("depositAmountFormatted", formatCurrency(appointment.getDepositAmount()));
        context.setVariable("consultationFeeFormatted", formatCurrency(appointment.getConsultationFee()));
        context.setVariable("detailUrl", frontendUrl + "/history");

        return context;
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null)
            return "0 VNĐ";
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return currencyFormat.format(amount) + " VNĐ";
    }
}
