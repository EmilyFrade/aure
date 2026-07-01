package com.aure.service;

import com.aure.api.dto.AuthResponseDto;
import com.aure.api.dto.ClientOtpRequestDto;
import com.aure.api.dto.ClientOtpVerifyDto;
import com.aure.domain.Client;
import com.aure.domain.ClientSession;
import com.aure.messaging.WhatsAppMessageSender;
import com.aure.repository.ClientRepository;
import com.aure.repository.ClientSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientAuthService {

	private static final Duration OTP_TTL = Duration.ofMinutes(10);
	private static final Duration SESSION_TTL = Duration.ofDays(30);

	private final ClientRepository clientRepository;
	private final ClientSessionRepository clientSessionRepository;
	private final WhatsAppMessageSender whatsAppSender;
	private final SecureRandom secureRandom = new SecureRandom();

	@Transactional
	public void requestOtp(ClientOtpRequestDto request) {
		String phone = request.phone().trim();

		Client client = clientRepository.findByPhone(phone)
				.orElseGet(() -> clientRepository.save(Client.builder().phone(phone).build()));

		String code = String.format("%06d", secureRandom.nextInt(1_000_000));

		clientSessionRepository.save(ClientSession.builder()
				.client(client)
				.otpCode(code)
				.otpExpiresAt(Instant.now().plus(OTP_TTL))
				.build());

		whatsAppSender.sendOtp(phone, code);
	}

	@Transactional
	public AuthResponseDto verifyOtp(ClientOtpVerifyDto request) {
		String phone = request.phone().trim();

		Client client = clientRepository.findByPhone(phone)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Código inválido"));

		ClientSession session = clientSessionRepository
				.findTopByClientAndOtpVerifiedAtIsNullAndOtpExpiresAtAfterOrderByCreatedAtDesc(client, Instant.now())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Código inválido"));

		if (!session.getOtpCode().equals(request.code())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Código inválido");
		}

		Instant now = Instant.now();
		session.setOtpVerifiedAt(now);
		session.setSessionToken(UUID.randomUUID());
		session.setSessionExpiresAt(now.plus(SESSION_TTL));
		session.setLastLoginAt(now);
		clientSessionRepository.save(session);

		return new AuthResponseDto(session.getSessionToken(), null);
	}
}
