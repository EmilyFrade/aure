package com.aure.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("whatsapp")
public record WhatsAppProperties(
		String provider,
		String frontendUrl,
		Evolution evolution
) {

	public record Evolution(String url, String apiKey, String instance) {
	}
}
