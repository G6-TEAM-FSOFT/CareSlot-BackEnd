package com.org.care_slot.service;

public interface EmailService {
    void sendAppointmentConfirmation(Long appointmentId);
    void sendAppointmentCancellation(Long appointmentId);
    void sendAppointmentReminder(Long appointmentId);
}
