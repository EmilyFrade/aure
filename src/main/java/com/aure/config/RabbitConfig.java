package com.aure.config;

import com.aure.messaging.AppointmentEventPublisher;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

	@Bean
	public TopicExchange aureEventsExchange() {
		return new TopicExchange(AppointmentEventPublisher.EXCHANGE, true, false);
	}

	@Bean
	public Queue appointmentCreatedWhatsAppQueue() {
		return new Queue(AppointmentEventPublisher.QUEUE_CREATED_WHATSAPP, true);
	}

	@Bean
	public Binding appointmentCreatedWhatsAppBinding(Queue appointmentCreatedWhatsAppQueue, TopicExchange aureEventsExchange) {
		return BindingBuilder.bind(appointmentCreatedWhatsAppQueue)
				.to(aureEventsExchange)
				.with(AppointmentEventPublisher.ROUTING_KEY);
	}

	@Bean
	public Queue appointmentCancelledWhatsAppQueue() {
		return new Queue(AppointmentEventPublisher.QUEUE_CANCELLED_WHATSAPP, true);
	}

	@Bean
	public Binding appointmentCancelledWhatsAppBinding(Queue appointmentCancelledWhatsAppQueue, TopicExchange aureEventsExchange) {
		return BindingBuilder.bind(appointmentCancelledWhatsAppQueue)
				.to(aureEventsExchange)
				.with(AppointmentEventPublisher.ROUTING_KEY_CANCELLED);
	}

	@Bean
	public Queue appointmentCancelledWaitingListQueue() {
		return new Queue(AppointmentEventPublisher.QUEUE_CANCELLED_WAITING_LIST, true);
	}

	@Bean
	public Binding appointmentCancelledWaitingListBinding(Queue appointmentCancelledWaitingListQueue, TopicExchange aureEventsExchange) {
		return BindingBuilder.bind(appointmentCancelledWaitingListQueue)
				.to(aureEventsExchange)
				.with(AppointmentEventPublisher.ROUTING_KEY_CANCELLED);
	}

	@Bean
	public JacksonJsonMessageConverter messageConverter() {
		return new JacksonJsonMessageConverter();
	}
}
