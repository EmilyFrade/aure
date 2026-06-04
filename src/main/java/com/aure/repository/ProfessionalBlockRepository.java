package com.aure.repository;

import com.aure.domain.ProfessionalBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProfessionalBlockRepository extends JpaRepository<ProfessionalBlock, Long> {

	List<ProfessionalBlock> findByProfessionalId(Long professionalId);

	Optional<ProfessionalBlock> findByIdAndProfessionalId(Long id, Long professionalId);

	List<ProfessionalBlock> findByProfessionalIdAndStartDatetimeLessThanAndEndDatetimeGreaterThan(
		Long professionalId, LocalDateTime upperBound, LocalDateTime lowerBound
	);
}
