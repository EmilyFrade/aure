package com.aure.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "whatsapp.provider", havingValue = "stub", matchIfMissing = true)
public class WhatsAppMessageSenderStub implements WhatsAppMessageSender {

	@Override
	public void sendOtp(String phone, String code) {
		log.info("[WhatsApp stub] OTP {} para {}", code, phone);
	}

	@Override
	public void notifyAppointmentCreated(AppointmentMessage message, boolean byProfessional) {
		log.info("[WhatsApp stub] Agendamento {}em {} com {} para {} em {} às {} - notificando {} ({})",
				byProfessional ? "manual " : "", message.brandName(), message.professionalName(), message.serviceName(),
				message.date(), message.time(), message.clientName(), message.phone());
	}

	@Override
	public void notifyAppointmentCancelled(AppointmentMessage message) {
		log.info("[WhatsApp stub] Cancelamento pelo profissional em {} com {} para {} em {} às {} - notificando {} ({})",
				message.brandName(), message.professionalName(), message.serviceName(),
				message.date(), message.time(), message.clientName(), message.phone());
	}

	@Override
	public void notifyWaitingListSlot(AppointmentMessage message) {
		log.info("[WhatsApp stub] Vaga liberada em {} com {} para {} em {} às {} - notificando {} ({})",
				message.brandName(), message.professionalName(), message.serviceName(),
				message.date(), message.time(), message.clientName(), message.phone());
	}

	@Override
	public boolean sendAppointmentReminder(AppointmentMessage message) {
		log.info("[WhatsApp stub] Lembrete de {} com {} para {} em {} às {} - notificando {} ({})",
				message.brandName(), message.professionalName(), message.serviceName(),
				message.date(), message.time(), message.clientName(), message.phone());
		return true;
	}

	@Override
	public void notifyReminderConfirmed(AppointmentMessage message) {
		log.info("[WhatsApp stub] Presença confirmada em {} com {} em {} às {} - notificando {} ({})",
				message.brandName(), message.professionalName(), message.date(), message.time(), message.clientName(), message.phone());
	}

	@Override
	public void notifyReminderCancelled(AppointmentMessage message) {
		log.info("[WhatsApp stub] Cancelado pelo lembrete em {} com {} em {} às {} - notificando {} ({})",
				message.brandName(), message.professionalName(), message.date(), message.time(), message.clientName(), message.phone());
	}
}
