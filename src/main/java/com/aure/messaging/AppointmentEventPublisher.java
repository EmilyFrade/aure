package com.aure.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppointmentEventPublisher {

	public static final String EXCHANGE = "aure.events";
	public static final String ROUTING_KEY = "appointment.created";
	public static final String ROUTING_KEY_CANCELLED = "appointment.cancelled";

	private final RabbitTemplate rabbitTemplate;

	public void publish(AppointmentCreatedEvent event) {
		rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
	}

	public void publish(AppointmentCancelledEvent event) {
		rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY_CANCELLED, event);
	}
}
