package com.aure.service;

import com.aure.domain.Appointment;
import com.aure.domain.AppointmentReminder;
import com.aure.domain.AppointmentStatus;
import com.aure.domain.ReminderResponse;
import com.aure.domain.ReminderStatus;
import com.aure.messaging.AppointmentMessage;
import com.aure.messaging.WhatsAppMessageSender;
import com.aure.repository.AppointmentReminderRepository;
import com.aure.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ReminderResponseService {

	private static final Set<AppointmentStatus> ACTIVE_STATUSES = Set.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);
	private static final Set<String> CONFIRM_ANSWERS = Set.of("1", "sim", "confirmar", "confirmo", "confirmado");
	private static final Set<String> CANCEL_ANSWERS = Set.of("2", "nao", "cancelar", "cancelo", "cancela");

	private final ClientRepository clientRepository;
	private final AppointmentReminderRepository appointmentReminderRepository;
	private final AppointmentService appointmentService;
	private final WhatsAppMessageSender whatsAppMessageSender;

	@Transactional
	public void handleReply(String whatsAppNumber, String text) {
		ReminderResponse response = parse(text);
		if (response == null) return;

		Optional<AppointmentReminder> pending = clientRepository.findByPhoneDigits(phoneCandidates(whatsAppNumber))
				.flatMap(client -> appointmentReminderRepository
						.findAwaitingResponse(client.getId(), ReminderStatus.SENT, ACTIVE_STATUSES).stream()
						.filter(reminder -> isUpcoming(reminder.getAppointment()))
						.findFirst());
		if (pending.isEmpty()) return;

		AppointmentReminder reminder = pending.get();
		Appointment appointment = reminder.getAppointment();
		if (response == ReminderResponse.CONFIRMED) {
			appointmentService.confirmByReminder(appointment);
		} else {
			appointmentService.cancelByReminder(appointment);
		}
		reminder.setResponse(response);
		reminder.setRespondedAt(Instant.now());
		appointmentReminderRepository.save(reminder);

		AppointmentMessage message = AppointmentMessage.of(appointment);
		if (response == ReminderResponse.CONFIRMED) {
			whatsAppMessageSender.notifyReminderConfirmed(message);
		} else {
			whatsAppMessageSender.notifyReminderCancelled(message);
		}
	}

	private static ReminderResponse parse(String text) {
		String answer = Normalizer.normalize(text.trim().toLowerCase(), Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.replaceAll("[^a-z0-9]", "");
		if (CONFIRM_ANSWERS.contains(answer)) return ReminderResponse.CONFIRMED;
		if (CANCEL_ANSWERS.contains(answer)) return ReminderResponse.CANCELLED;
		return null;
	}

	private static Set<String> phoneCandidates(String whatsAppNumber) {
		String digits = whatsAppNumber.replaceAll("\\D", "");
		String national = digits.startsWith("55") && digits.length() > 11 ? digits.substring(2) : digits;
		Set<String> candidates = new LinkedHashSet<>();
		candidates.add(national);
		if (national.length() == 10) {
			candidates.add(national.substring(0, 2) + "9" + national.substring(2));
		}
		for (String candidate : Set.copyOf(candidates)) {
			candidates.add("55" + candidate);
		}
		return candidates;
	}

	private static boolean isUpcoming(Appointment appointment) {
		return LocalDateTime.of(appointment.getScheduledDate(), appointment.getScheduledTime()).isAfter(LocalDateTime.now());
	}
}
