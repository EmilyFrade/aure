package com.aure.messaging;

public interface WhatsAppMessageSender {

	void sendOtp(String phone, String code);

	void notifyAppointmentCreated(AppointmentMessage message, boolean byProfessional);

	void notifyAppointmentCancelled(AppointmentMessage message);

	void notifyWaitingListSlot(AppointmentMessage message);
}
