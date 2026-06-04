package com.aure.repository;

import com.aure.domain.ProfessionalSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfessionalSessionRepository extends JpaRepository<ProfessionalSession, UUID> {

	@EntityGraph(attributePaths = {"user", "user.professional"})
	Optional<ProfessionalSession> findByToken(UUID token);

	void deleteByToken(UUID token);

}
