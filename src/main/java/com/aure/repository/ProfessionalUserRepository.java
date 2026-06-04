package com.aure.repository;

import com.aure.domain.ProfessionalUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfessionalUserRepository extends JpaRepository<ProfessionalUser, Long> {

	Optional<ProfessionalUser> findByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByProfessionalId(Long professionalId);

}
