package com.aure.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@ConditionalOnProperty(name = "whatsapp.provider", havingValue = "evolution")
public class EvolutionKeepAlive {

	private final RestClient restClient;

	public EvolutionKeepAlive(WhatsAppProperties properties) {
		this.restClient = RestClient.builder().baseUrl(properties.evolution().url()).build();
	}

	@Scheduled(fixedDelayString = "${whatsapp.evolution.keep-alive-interval}")
	public void ping() {
		try {
			restClient.get().uri("/").retrieve().toBodilessEntity();
		} catch (RestClientException e) {
			log.warn("[WhatsApp] Evolution não respondeu ao ping: {}", e.getMessage());
		}
	}
}
