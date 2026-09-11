package com.org.care_slot.service;

import com.org.care_slot.entity.Appointment;
import com.org.care_slot.entity.AppointmentSlot;
import com.org.care_slot.entity.Clinic;
import com.org.care_slot.entity.Doctor;
import com.org.care_slot.entity.PatientProfile;
import com.org.care_slot.entity.Specialty;
import com.org.care_slot.entity.User;
import com.org.care_slot.repository.AppointmentRepository;
import com.org.care_slot.service.impl.EmailServiceImpl;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private EmailServiceImpl emailService;

    private Appointment sampleAppointment;
    private final Long appointmentId = 100L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "no-reply@careslot.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:5173");

        User user = new User();
        user.setEmail("patient@example.com");
        user.setFullName("Nguyen Van A");

        PatientProfile patientProfile = new PatientProfile();
        patientProfile.setUser(user);
        patientProfile.setFullName("Nguyen Van A");
        patientProfile.setPhone("0987654321");

        Clinic clinic = new Clinic();
        clinic.setName("CareSlot Clinic");
        clinic.setAddress("123 Duong 3/2, Q10, TP.HCM");
        clinic.setPhone("19001234");

        Specialty specialty = new Specialty();
        specialty.setName("Noi khoa");

        Doctor doctor = new Doctor();
        doctor.setFullName("Dr. Nguyen Van B");
        doctor.setTitle("Thac si, Bac si");
        doctor.setClinic(clinic);
        doctor.setSpecialty(specialty);

        AppointmentSlot slot = new AppointmentSlot();
        slot.setDoctor(doctor);
        slot.setAppointmentDate(LocalDate.of(2026, 9, 15));
        slot.setStartTime(LocalTime.of(9, 0));
        slot.setEndTime(LocalTime.of(9, 30));
        slot.setRoomName("Phong 102");

        sampleAppointment = new Appointment();
        sampleAppointment.setId(appointmentId);
        sampleAppointment.setBookingCode("CS-20260907-001");
        sampleAppointment.setPatientProfile(patientProfile);
        sampleAppointment.setSlot(slot);
        sampleAppointment.setDepositAmount(BigDecimal.valueOf(100000));
        sampleAppointment.setConsultationFee(BigDecimal.valueOf(300000));
    }

    @Test
    @DisplayName("Should successfully send confirmation email when appointment exists and has valid email")
    void testSendAppointmentConfirmation_Success() {
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(sampleAppointment));
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/appointment-confirmation"), any(Context.class)))
                .thenReturn("<html><body>Mock Email Content</body></html>");

        emailService.sendAppointmentConfirmation(appointmentId);

        verify(appointmentRepository, times(1)).findById(appointmentId);
        verify(templateEngine, times(1)).process(eq("email/appointment-confirmation"), any(Context.class));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should skip sending email when appointmentId is null or not found")
    void testSendAppointmentConfirmation_NotFound() {
        when(appointmentRepository.findById(999L)).thenReturn(Optional.empty());

        emailService.sendAppointmentConfirmation(999L);
        emailService.sendAppointmentConfirmation(null);

        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should skip sending email when recipient email is null or blank")
    void testSendAppointmentConfirmation_NoRecipientEmail() {
        sampleAppointment.getPatientProfile().getUser().setEmail(null);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(sampleAppointment));

        emailService.sendAppointmentConfirmation(appointmentId);

        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should catch and isolate SMTP exception without crashing (Failure Isolation)")
    void testSendAppointmentConfirmation_SmtpFailure_ShouldNotThrow() {
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(sampleAppointment));
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/appointment-confirmation"), any(Context.class)))
                .thenReturn("<html><body>Mock Email Content</body></html>");
        doThrow(new RuntimeException("SMTP server connection timeout")).when(mailSender).send(any(MimeMessage.class));

        assertDoesNotThrow(() -> emailService.sendAppointmentConfirmation(appointmentId));
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
