package com.aure.repository;

import com.aure.domain.Client;
import com.aure.domain.ClientSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ClientSessionRepository extends JpaRepository<ClientSession, Long> {

	@EntityGraph(attributePaths = {"client"})
	Optional<ClientSession> findBySessionToken(UUID sessionToken);

	Optional<ClientSession> findTopByClientAndOtpVerifiedAtIsNullAndOtpExpiresAtAfterOrderByCreatedAtDesc(Client client, Instant now);
}
