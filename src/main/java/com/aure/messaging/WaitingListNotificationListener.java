package com.aure.messaging;

import com.aure.service.WaitingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WaitingListNotificationListener {

	private final WaitingListService waitingListService;

	@RabbitListener(queues = AppointmentEventPublisher.QUEUE_CANCELLED_WAITING_LIST)
	public void onAppointmentCancelled(AppointmentCancelledEvent event) {
		waitingListService.notifyNextInQueue(event);
	}
}
