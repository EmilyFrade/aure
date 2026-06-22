package com.aure.messaging;

import com.aure.service.WaitingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WaitingListNotificationListener {

	private final WaitingListService waitingListService;

	@RabbitListener(queues = AppointmentEventPublisher.ROUTING_KEY_CANCELLED)
	public void onAppointmentCancelled(AppointmentCancelledEvent event) {
		waitingListService.notifyNextInQueue(event);
	}
}
