package com.aure.service;

import com.aure.domain.Appointment;
import com.aure.domain.AppointmentReminder;
import com.aure.domain.AppointmentStatus;
import com.aure.domain.ReminderStatus;
import com.aure.messaging.AppointmentMessage;
import com.aure.messaging.WhatsAppMessageSender;
import com.aure.repository.AppointmentReminderRepository;
import com.aure.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AppointmentReminderService {

	private static final int HOURS_BEFORE = 24;
	private static final Set<AppointmentStatus> ACTIVE_STATUSES = Set.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);

	private final AppointmentRepository appointmentRepository;
	private final AppointmentReminderRepository appointmentReminderRepository;
	private final WhatsAppMessageSender whatsAppMessageSender;

	@Scheduled(fixedDelayString = "${reminder.interval}")
	public void sendDueReminders() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime limit = now.plusHours(HOURS_BEFORE);

		List<Appointment> appointments = appointmentRepository.findWithoutReminder(
				ACTIVE_STATUSES, now.toLocalDate(), limit.toLocalDate());

		for (Appointment appointment : appointments) {
			LocalDateTime start = LocalDateTime.of(appointment.getScheduledDate(), appointment.getScheduledTime());
			if (!start.isAfter(now) || start.isAfter(limit) || bookedWithinLeadTime(appointment, start)) continue;

			boolean sent = whatsAppMessageSender.sendAppointmentReminder(AppointmentMessage.of(appointment));
			appointmentReminderRepository.save(AppointmentReminder.builder()
					.appointment(appointment)
					.status(sent ? ReminderStatus.SENT : ReminderStatus.FAILED)
					.build());
		}
	}

	private static boolean bookedWithinLeadTime(Appointment appointment, LocalDateTime start) {
		return appointment.getCreatedAt().isAfter(start.minusHours(HOURS_BEFORE).atZone(ZoneId.systemDefault()).toInstant());
	}
}
