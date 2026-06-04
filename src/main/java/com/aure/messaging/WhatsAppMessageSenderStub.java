package com.aure.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WhatsAppMessageSenderStub implements WhatsAppMessageSender {

	@Override
	public void sendOtp(String phone, String code) {
		log.info("[WhatsApp stub] OTP {} para {}", code, phone);
	}
}
