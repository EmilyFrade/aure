package com.aure.repository;

import com.aure.domain.WaitingList;
import com.aure.domain.WaitingListStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WaitingListRepository extends JpaRepository<WaitingList, Long> {

	@EntityGraph(attributePaths = {"professional", "client", "service"})
	List<WaitingList> findByProfessionalIdOrderByPositionAsc(Long professionalId);

	Optional<WaitingList> findByIdAndClientId(Long id, Long clientId);

	Optional<WaitingList> findByIdAndProfessionalId(Long id, Long professionalId);

	int countByProfessionalIdAndServiceIdAndStatus(Long professionalId, Long serviceId, WaitingListStatus status);

	@Query("SELECT w FROM WaitingList w WHERE w.professional.id = :professionalId AND w.service.id = :serviceId " +
			"AND w.status = :status " +
			"AND w.preferredDateStart <= :date AND w.preferredDateEnd >= :date " +
			"ORDER BY w.position ASC")
	List<WaitingList> findMatchingWaitingEntries(
			@Param("professionalId") Long professionalId,
			@Param("serviceId") Long serviceId,
			@Param("status") WaitingListStatus status,
			@Param("date") LocalDate date);
}
