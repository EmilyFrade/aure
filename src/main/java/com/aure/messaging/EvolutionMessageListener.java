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

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "whatsapp.provider", havingValue = "evolution")
public class EvolutionMessageListener {

	private static final String CONTACT_SUFFIX = "@s.whatsapp.net";

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
		String remoteJid = key.path("remoteJid").asString("");
		if (!remoteJid.endsWith(CONTACT_SUFFIX)) return;
		if (key.path("fromMe").asBoolean() && !remoteJid.equals(root.path("sender").asString(""))) return;

		String text = root.path("data").path("message").path("conversation").asString("");
		if (text.isBlank()) return;

		reminderResponseService.handleReply(remoteJid.substring(0, remoteJid.length() - CONTACT_SUFFIX.length()), text);
	}
}
