package com.aure.messaging;

public interface WhatsAppMessageSender {

	void sendOtp(String phone, String code);
}
