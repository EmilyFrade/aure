package com.aure.messaging;

import com.aure.service.ReminderResponseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EvolutionMessageListenerTest {

	private static final String CLIENT_JID = "5531999990000@s.whatsapp.net";

	@Mock ReminderResponseService reminderResponseService;

	private EvolutionMessageListener listener;

	@BeforeEach
	void setUp() {
		listener = new EvolutionMessageListener(JsonMapper.builder().build(), reminderResponseService);
	}

	@Test
	@DisplayName("resposta digitada chega ao serviço de respostas")
	void plainReply() {
		receive(CLIENT_JID, false, """
				{"conversation": "1"}
				""");

		verify(reminderResponseService).handleReply("5531999990000", "1");
	}

	@Test
	@DisplayName("resposta citando o lembrete também é lida")
	void quotedReply() {
		receive(CLIENT_JID, false, """
				{"extendedTextMessage": {"text": "2", "contextInfo": {"quotedMessage": {"conversation": "Lembrete"}}}}
				""");

		verify(reminderResponseService).handleReply("5531999990000", "2");
	}

	@Test
	@DisplayName("contato identificado por @lid usa o número de remoteJidAlt")
	void lidContact() {
		receiveRaw("""
				{"sender": "5531988880000@s.whatsapp.net",
				 "data": {"key": {"remoteJid": "118463855108140@lid", "remoteJidAlt": "5531999990000@s.whatsapp.net",
				                  "fromMe": false, "addressingMode": "lid"},
				          "message": {"conversation": "1"}}}
				""");

		verify(reminderResponseService).handleReply("5531999990000", "1");
	}

	@Test
	@DisplayName("resposta em conversa com mensagens temporárias também é lida")
	void ephemeralReply() {
		receive(CLIENT_JID, false, """
				{"ephemeralMessage": {"message": {"extendedTextMessage": {"text": "2"}}}}
				""");

		verify(reminderResponseService).handleReply("5531999990000", "2");
	}

	@Test
	@DisplayName("mensagens de grupo e sem texto são ignoradas")
	void ignoredMessages() {
		receive("123456789@g.us", false, """
				{"conversation": "1"}
				""");
		receive(CLIENT_JID, false, """
				{"imageMessage": {"caption": ""}}
				""");

		verifyNoInteractions(reminderResponseService);
	}

	@Test
	@DisplayName("mensagem enviada pelo próprio número para outra pessoa é ignorada")
	void ownMessageToOtherContact() {
		receive(CLIENT_JID, true, """
				{"conversation": "1"}
				""");

		verifyNoInteractions(reminderResponseService);
	}

	private void receive(String remoteJid, boolean fromMe, String message) {
		String body = """
				{"sender": "5531988880000@s.whatsapp.net",
				 "data": {"key": {"remoteJid": "%s", "fromMe": %s}, "message": %s}}
				""".formatted(remoteJid, fromMe, message);
		receiveRaw(body);
	}

	private void receiveRaw(String body) {
		listener.onMessageReceived(new Message(body.getBytes(StandardCharsets.UTF_8)));
	}
}
