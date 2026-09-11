package com.org.care_slot.event;

import com.org.care_slot.enums.AppointmentEventType;

public record AppointmentEvent(
    Long appointmentId,
    AppointmentEventType eventType
) {}
