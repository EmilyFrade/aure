package com.aure.messaging;

import java.time.LocalDate;
import java.time.LocalTime;

public interface WhatsAppMessageSender {

	void sendOtp(String phone, String code);

	void notifyWaitingListSlot(String phone, String professionalName, String serviceName, LocalDate date, LocalTime time);
}
