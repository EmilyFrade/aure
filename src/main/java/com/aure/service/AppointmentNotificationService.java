package com.aure.service;

import com.aure.domain.Appointment;
import com.aure.domain.AppointmentStatus;
import com.aure.messaging.AppointmentCancelledEvent;
import com.aure.messaging.AppointmentCreatedEvent;
import com.aure.messaging.AppointmentMessage;
import com.aure.messaging.WhatsAppMessageSender;
import com.aure.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AppointmentNotificationService {

	private final AppointmentRepository appointmentRepository;
	private final WhatsAppMessageSender whatsAppMessageSender;

	public void notifyCreated(AppointmentCreatedEvent event) {
		appointmentRepository.findByIdWithDetails(event.appointmentId())
				.filter(appointment -> appointment.getStatus() != AppointmentStatus.CANCELLED)
				.filter(AppointmentNotificationService::isUpcoming)
				.ifPresent(appointment -> whatsAppMessageSender.notifyAppointmentCreated(
						AppointmentMessage.of(appointment), event.byProfessional()));
	}

	public void notifyCancelled(AppointmentCancelledEvent event) {
		if (!event.byProfessional()) return;
		appointmentRepository.findByIdWithDetails(event.appointmentId())
				.filter(AppointmentNotificationService::isUpcoming)
				.ifPresent(appointment -> whatsAppMessageSender.notifyAppointmentCancelled(AppointmentMessage.of(appointment)));
	}

	private static boolean isUpcoming(Appointment appointment) {
		return LocalDateTime.of(appointment.getScheduledDate(), appointment.getScheduledTime()).isAfter(LocalDateTime.now());
	}
}
