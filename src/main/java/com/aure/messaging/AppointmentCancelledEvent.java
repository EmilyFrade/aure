package com.aure.messaging;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentCancelledEvent(
		Long appointmentId,
		Long professionalId,
		Long clientId,
		Long serviceId,
		LocalDate scheduledDate,
		LocalTime scheduledTime
) {}
