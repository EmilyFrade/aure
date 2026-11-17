package com.aure.messaging;

import com.aure.config.EvolutionRabbitConfig;
import com.aure.service.ReminderResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "whatsapp.provider", havingValue = "evolution")
public class EvolutionMessageListener {

	private static final String CONTACT_SUFFIX = "@s.whatsapp.net";
	private static final List<String> CONTACT_FIELDS = List.of("remoteJid", "remoteJidAlt", "senderPn");

	private final JsonMapper jsonMapper;
	private final ReminderResponseService reminderResponseService;

	@RabbitListener(queues = EvolutionRabbitConfig.QUEUE_MESSAGES_UPSERT)
	public void onMessageReceived(Message message) {
		try {
			handle(jsonMapper.readTree(message.getBody()));
		} catch (RuntimeException e) {
			log.error("[WhatsApp] falha ao processar mensagem recebida pela Evolution: {}", e.getMessage());
		}
	}

	private void handle(JsonNode root) {
		JsonNode key = root.path("data").path("key");
		String contactJid = contactJid(key);
		if (contactJid == null) return;
		if (key.path("fromMe").asBoolean() && !contactJid.equals(root.path("sender").asString(""))) return;

		JsonNode content = root.path("data").path("message");
		if (content.has("ephemeralMessage")) content = content.path("ephemeralMessage").path("message");
		String text = content.path("conversation").asString("");
		if (text.isBlank()) text = content.path("extendedTextMessage").path("text").asString("");
		if (text.isBlank()) return;

		reminderResponseService.handleReply(contactJid.substring(0, contactJid.length() - CONTACT_SUFFIX.length()), text);
	}

	private static String contactJid(JsonNode key) {
		for (String field : CONTACT_FIELDS) {
			String jid = key.path(field).asString("");
			if (jid.endsWith(CONTACT_SUFFIX)) return jid;
		}
		return null;
	}
}
