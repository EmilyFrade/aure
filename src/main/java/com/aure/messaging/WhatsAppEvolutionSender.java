package com.aure.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty(name = "whatsapp.provider", havingValue = "evolution")
@EnableConfigurationProperties(WhatsAppProperties.class)
public class WhatsAppEvolutionSender implements WhatsAppMessageSender {

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, dd/MM", Locale.of("pt", "BR"));
	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

	private final WhatsAppProperties.Evolution config;
	private final String frontendUrl;
	private final Set<String> allowedNumbers;
	private final RestClient restClient;

	public WhatsAppEvolutionSender(WhatsAppProperties properties) {
		this.config = properties.evolution();
		this.frontendUrl = properties.frontendUrl();
		this.allowedNumbers = properties.allowedNumbers() == null ? Set.of() : properties.allowedNumbers().stream()
				.filter(number -> !number.isBlank())
				.map(WhatsAppEvolutionSender::toWhatsAppNumber)
				.collect(Collectors.toSet());
		if (allowedNumbers.isEmpty()) {
			log.warn("[WhatsApp] WHATSAPP_ALLOWED_NUMBERS vazio: nenhuma mensagem será enviada pela Evolution");
		}
		this.restClient = RestClient.builder()
				.baseUrl(config.url())
				.defaultHeader("apikey", config.apiKey())
				.requestFactory(requestFactory())
				.build();
	}

	@Override
	public void sendOtp(String phone, String code) {
		String text = "Seu código de acesso ao *Aure* é:\n\n"
				+ "*" + code + "*\n\n"
				+ "Ele vale por 10 minutos. Se você não pediu este código, ignore esta mensagem.";

		if (!sendText(phone, text)) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Não foi possível enviar o código pelo WhatsApp. Tente de novo em instantes.");
		}
	}

	@Override
	public void notifyAppointmentCreated(AppointmentMessage message, boolean byProfessional) {
		String intro = byProfessional
				? "Um horário foi agendado para você em *" + message.brandName() + "*. Confira os detalhes:"
				: "Seu horário em *" + message.brandName() + "* está agendado. Confira os detalhes:";
		String text = greeting(message.clientName()) + "\n"
				+ intro + "\n\n"
				+ details(message) + "\n\n"
				+ "Para ver ou cancelar, acesse:\n"
				+ frontendUrl + "/meus-agendamentos";

		sendText(message.phone(), text);
	}

	@Override
	public void notifyAppointmentCancelled(AppointmentMessage message) {
		String text = greeting(message.clientName()) + "\n"
				+ "*" + message.brandName() + "* precisou cancelar o seu horário:\n\n"
				+ details(message) + "\n\n"
				+ "Se quiser, escolha outro horário por aqui:\n"
				+ frontendUrl + "/agendar/" + message.brandSlug() + "?profissional=" + message.professionalId();

		sendText(message.phone(), text);
	}

	@Override
	public void notifyWaitingListSlot(AppointmentMessage message) {
		String text = greeting(message.clientName()) + "\n"
				+ "Abriu uma vaga em *" + message.brandName() + "* para o serviço que você estava esperando. Confira os detalhes:\n\n"
				+ details(message) + "\n\n"
				+ "Para garantir a vaga, agende por aqui:\n"
				+ frontendUrl + "/meus-agendamentos\n\n"
				+ "A vaga fica com quem agendar primeiro.";

		sendText(message.phone(), text);
	}

	private boolean sendText(String phone, String text) {
		String to = toWhatsAppNumber(phone);
		if (!allowedNumbers.contains(to)) {
			log.warn("[WhatsApp] envio bloqueado para {}: número fora de WHATSAPP_ALLOWED_NUMBERS", to);
			return false;
		}

		try {
			restClient.post()
					.uri("/message/sendText/{instance}", config.instance())
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of("number", to, "text", text))
					.retrieve()
					.toBodilessEntity();
			log.info("[WhatsApp] mensagem enviada para {} pela Evolution", to);
			return true;
		} catch (RestClientResponseException e) {
			log.error("[WhatsApp] falha ao enviar para {} pela Evolution: {} {}", to, e.getStatusCode(), e.getResponseBodyAsString());
		} catch (RestClientException e) {
			log.error("[WhatsApp] falha ao enviar para {} pela Evolution: {}", to, e.getMessage());
		}
		return false;
	}

	private static String details(AppointmentMessage message) {
		return "📅 *Serviço*\n" + message.serviceName() + "\n"
				+ "🕐 *Horário*\n" + describeDay(message.date()) + ", às " + message.time().format(TIME_FORMAT) + "\n"
				+ "📍 *Profissional*\n" + message.professionalName();
	}

	private static String greeting(String clientName) {
		if (clientName == null || clientName.isBlank()) return "Olá!";
		return "Olá, *" + clientName.trim().split("\\s+")[0] + "*!";
	}

	private static String describeDay(LocalDate date) {
		String formatted = date.format(DATE_FORMAT);
		return Character.toUpperCase(formatted.charAt(0)) + formatted.substring(1);
	}

	private static String toWhatsAppNumber(String phone) {
		String digits = phone.replaceAll("\\D", "");
		return digits.length() <= 11 ? "55" + digits : digits;
	}

	private static JdkClientHttpRequestFactory requestFactory() {
		HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
		factory.setReadTimeout(Duration.ofSeconds(15));
		return factory;
	}
}
