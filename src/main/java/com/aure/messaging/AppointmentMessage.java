package com.aure.messaging;

import com.aure.domain.Appointment;
import com.aure.domain.Client;
import com.aure.domain.Professional;
import com.aure.domain.Service;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentMessage(
		String phone,
		String clientName,
		String brandName,
		String brandSlug,
		Long professionalId,
		String professionalName,
		String serviceName,
		LocalDate date,
		LocalTime time
) {

	public static AppointmentMessage of(Client client, Professional professional, Service service, LocalDate date, LocalTime time) {
		return new AppointmentMessage(
				client.getPhone(),
				client.getName(),
				professional.getBrand().getName(),
				professional.getBrand().getSlug(),
				professional.getId(),
				professional.getName(),
				service.getName(),
				date,
				time);
	}

	public static AppointmentMessage of(Appointment appointment) {
		return of(appointment.getClient(), appointment.getProfessional(), appointment.getService(),
				appointment.getScheduledDate(), appointment.getScheduledTime());
	}
}
