package com.aure.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@Component
@ConditionalOnProperty(name = "whatsapp.provider", havingValue = "stub", matchIfMissing = true)
public class WhatsAppMessageSenderStub implements WhatsAppMessageSender {

	@Override
	public void sendOtp(String phone, String code) {
		log.info("[WhatsApp stub] OTP {} para {}", code, phone);
	}

	@Override
	public void notifyWaitingListSlot(String phone, String clientName, String brandName, String professionalName, String serviceName, LocalDate date, LocalTime time) {
		log.info("[WhatsApp stub] Vaga liberada em {} com {} para {} em {} às {} - notificando {} ({})", brandName, professionalName, serviceName, date, time, clientName, phone);
	}
}
