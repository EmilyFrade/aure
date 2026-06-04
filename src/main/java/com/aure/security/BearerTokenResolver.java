package com.aure.security;

import java.util.Optional;
import java.util.UUID;

public final class BearerTokenResolver {

	private static final String BEARER_PREFIX = "Bearer ";

	public static Optional<UUID> resolve(String authorizationHeader) {
		if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
			return Optional.empty();
		}

		String rawToken = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
		if (rawToken.isEmpty()) {
			return Optional.empty();
		}

		try {
			return Optional.of(UUID.fromString(rawToken));
		} catch (IllegalArgumentException ex) {
			return Optional.empty();
		}
	}
}
