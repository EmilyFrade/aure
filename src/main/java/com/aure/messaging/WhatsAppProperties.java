package com.aure.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties("whatsapp")
public record WhatsAppProperties(
		String provider,
		String frontendUrl,
		List<String> allowedNumbers,
		Evolution evolution
) {

	public record Evolution(String url, String apiKey, String instance) {
	}
}
