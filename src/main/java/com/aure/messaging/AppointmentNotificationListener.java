package com.aure.messaging;

import com.aure.service.AppointmentNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentNotificationListener {

	private final AppointmentNotificationService appointmentNotificationService;

	@RabbitListener(queues = AppointmentEventPublisher.QUEUE_CREATED_WHATSAPP)
	public void onAppointmentCreated(AppointmentCreatedEvent event) {
		appointmentNotificationService.notifyCreated(event);
	}

	@RabbitListener(queues = AppointmentEventPublisher.QUEUE_CANCELLED_WHATSAPP)
	public void onAppointmentCancelled(AppointmentCancelledEvent event) {
		appointmentNotificationService.notifyCancelled(event);
	}
}
