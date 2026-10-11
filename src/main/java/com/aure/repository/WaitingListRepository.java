package com.aure.repository;

import com.aure.domain.WaitingList;
import com.aure.domain.WaitingListStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WaitingListRepository extends JpaRepository<WaitingList, Long> {

	@EntityGraph(attributePaths = {"professional", "professional.brand", "client", "service"})
	@Query("SELECT w FROM WaitingList w WHERE w.professional.id = :professionalId AND w.status IN :statuses " +
			"AND w.preferredDateEnd >= :today ORDER BY w.id ASC")
	List<WaitingList> findActiveByProfessional(
			@Param("professionalId") Long professionalId,
			@Param("statuses") Collection<WaitingListStatus> statuses,
			@Param("today") LocalDate today);

	Optional<WaitingList> findByIdAndClientId(Long id, Long clientId);

	boolean existsByServiceId(Long serviceId);

	Optional<WaitingList> findByIdAndProfessionalId(Long id, Long professionalId);

	int countByProfessionalIdAndStatusAndIdLessThan(Long professionalId, WaitingListStatus status, Long id);

	@Query("SELECT w FROM WaitingList w WHERE w.client.id = :clientId AND w.professional.id = :professionalId " +
			"AND w.service.id = :serviceId AND w.status IN :statuses " +
			"AND w.preferredDateStart <= :date AND w.preferredDateEnd >= :date")
	List<WaitingList> findOpenForClientOnDate(
			@Param("clientId") Long clientId,
			@Param("professionalId") Long professionalId,
			@Param("serviceId") Long serviceId,
			@Param("statuses") Collection<WaitingListStatus> statuses,
			@Param("date") LocalDate date);

	@EntityGraph(attributePaths = {"professional", "client", "service"})
	@Query("SELECT w FROM WaitingList w WHERE w.professional.id = :professionalId AND w.service.id = :serviceId " +
			"AND w.status = :status " +
			"AND w.preferredDateStart <= :date AND w.preferredDateEnd >= :date " +
			"ORDER BY w.id ASC")
	List<WaitingList> findMatchingWaitingEntries(
			@Param("professionalId") Long professionalId,
			@Param("serviceId") Long serviceId,
			@Param("status") WaitingListStatus status,
			@Param("date") LocalDate date);
}
