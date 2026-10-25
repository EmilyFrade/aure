package com.aure.security;

import com.aure.domain.ProfessionalSession;
import com.aure.repository.ProfessionalSessionRepository;
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
public class AuthFilter extends OncePerRequestFilter {

	private final ProfessionalSessionRepository sessionRepository;

	@Override
	protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.startsWith("/auth/login");
	}

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain
	) throws ServletException, IOException {
		BearerTokenResolver.resolve(request.getHeader(HttpHeaders.AUTHORIZATION))
				.flatMap(sessionRepository::findByToken)
				.filter(session -> !session.isExpired() && session.getUser().isActive())
				.ifPresent(this::authenticate);

		filterChain.doFilter(request, response);
	}

	private void authenticate(ProfessionalSession session) {
		var user = session.getUser();

		var principal = new AuthenticatedProfessional(
				user.getId(),
				user.getProfessional().getId(),
				user.getEmail(),
				session.getToken());

		var authentication = new UsernamePasswordAuthenticationToken(
				principal,
				null,
				List.of(new SimpleGrantedAuthority("ROLE_PROFESSIONAL"))
		);

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
