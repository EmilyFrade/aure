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
	public Queue appointmentCreatedQueue() {
		return new Queue(AppointmentEventPublisher.ROUTING_KEY, true);
	}

	@Bean
	public Binding appointmentCreatedBinding(Queue appointmentCreatedQueue, TopicExchange aureEventsExchange) {
		return BindingBuilder.bind(appointmentCreatedQueue)
				.to(aureEventsExchange)
				.with(AppointmentEventPublisher.ROUTING_KEY);
	}

	@Bean
	public JacksonJsonMessageConverter messageConverter() {
		return new JacksonJsonMessageConverter();
	}
}
