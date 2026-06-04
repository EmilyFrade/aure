package com.aure.service;

import com.aure.api.dto.AuthResponseDto;
import com.aure.api.dto.LoginRequestDto;
import com.aure.api.dto.RegisterRequestDto;
import com.aure.domain.ProfessionalSession;
import com.aure.domain.ProfessionalUser;
import com.aure.repository.ProfessionalRepository;
import com.aure.repository.ProfessionalSessionRepository;
import com.aure.repository.ProfessionalUserRepository;
import com.aure.security.AuthenticatedProfessional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

	private static final Duration SESSION_TTL = Duration.ofDays(7);

	private final ProfessionalUserRepository userRepository;
	private final ProfessionalRepository professionalRepository;
	private final ProfessionalSessionRepository sessionRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public AuthResponseDto register(RegisterRequestDto request) {
		var professional = professionalRepository.findById(request.getProfessionalId())
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						"Profissional não encontrado com id %d".formatted(request.getProfessionalId())));

		if (userRepository.existsByProfessionalId(professional.getId())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Profissional já possui conta de acesso");
		}

		if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
		}

		var user = ProfessionalUser.builder()
				.professional(professional)
				.email(request.getEmail().trim().toLowerCase())
				.passwordHash(passwordEncoder.encode(request.getPassword()))
				.active(true)
				.build();

		user = userRepository.save(user);
		return new AuthResponseDto(createSession(user).getToken());
	}

	@Transactional
	public AuthResponseDto login(LoginRequestDto request) {
		var user = userRepository.findByEmailIgnoreCase(request.getEmail().trim())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));

		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
		}

		if (!user.isActive()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Conta inativa");
		}

		user.setLastLoginAt(Instant.now());
		userRepository.save(user);

		return new AuthResponseDto(createSession(user).getToken());
	}

	@Transactional
	public void logout() {
		var principal = currentProfessional();
		sessionRepository.deleteByToken(principal.sessionToken());
		SecurityContextHolder.clearContext();
	}

	private ProfessionalSession createSession(ProfessionalUser user) {
		var session = ProfessionalSession.builder()
				.token(UUID.randomUUID())
				.user(user)
				.expiresAt(Instant.now().plus(SESSION_TTL))
				.build();
		return sessionRepository.save(session);
	}

	private static AuthenticatedProfessional currentProfessional() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedProfessional principal)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não autenticado");
		}
		return principal;
	}

}
