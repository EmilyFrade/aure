package com.aure.security;

import com.aure.domain.ClientSession;
import com.aure.repository.ClientSessionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ClientAuthFilter extends OncePerRequestFilter {

	private final ClientSessionRepository sessionRepository;

	@Override
	protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.startsWith("/auth/client/request-otp") || path.startsWith("/auth/client/verify");
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain
	) throws ServletException, IOException {
		BearerTokenResolver.resolve(request.getHeader(HttpHeaders.AUTHORIZATION))
				.flatMap(sessionRepository::findBySessionToken)
				.filter(session -> !session.isSessionExpired())
				.ifPresent(this::authenticate);

		filterChain.doFilter(request, response);
	}

	private void authenticate(ClientSession session) {
		var principal = new AuthenticatedClient(session.getClient().getId(), session.getSessionToken());

		var authentication = new UsernamePasswordAuthenticationToken(
				principal,
				null,
				List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
		);

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
