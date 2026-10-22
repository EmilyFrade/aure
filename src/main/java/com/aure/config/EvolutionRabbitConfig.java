package com.aure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "whatsapp.provider", havingValue = "evolution")
public class EvolutionRabbitConfig {

	public static final String EXCHANGE = "evolution";
	public static final String ROUTING_KEY_MESSAGES_UPSERT = "messages.upsert";
	public static final String QUEUE_MESSAGES_UPSERT = "evolution.messages.upsert";

	@Bean
	public TopicExchange evolutionExchange() {
		return new TopicExchange(EXCHANGE, true, false);
	}

	@Bean
	public Queue evolutionMessagesUpsertQueue() {
		return QueueBuilder.durable(QUEUE_MESSAGES_UPSERT).quorum().build();
	}

	@Bean
	public Binding evolutionMessagesUpsertBinding(Queue evolutionMessagesUpsertQueue, TopicExchange evolutionExchange) {
		return BindingBuilder.bind(evolutionMessagesUpsertQueue)
				.to(evolutionExchange)
				.with(ROUTING_KEY_MESSAGES_UPSERT);
	}
}
