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

	@EntityGraph(attributePaths = {"professional", "professional.brand", "client", "service"})
	@Query("SELECT w FROM WaitingList w WHERE w.client.id = :clientId AND w.status IN :statuses " +
			"AND w.preferredDateEnd >= :today ORDER BY w.id ASC")
	List<WaitingList> findActiveByClient(
			@Param("clientId") Long clientId,
			@Param("statuses") Collection<WaitingListStatus> statuses,
			@Param("today") LocalDate today);

	@EntityGraph(attributePaths = {"professional", "professional.brand", "client", "service"})
	@Query("SELECT w FROM WaitingList w WHERE w.id = :id")
	Optional<WaitingList> findByIdWithDetails(@Param("id") Long id);

	Optional<WaitingList> findByIdAndClientId(Long id, Long clientId);

	boolean existsByServiceId(Long serviceId);

	Optional<WaitingList> findByIdAndProfessionalId(Long id, Long professionalId);

	int countByProfessionalIdAndStatusAndIdLessThan(Long professionalId, WaitingListStatus status, Long id);

	@Query("SELECT COUNT(w) > 0 FROM WaitingList w WHERE w.client.id = :clientId AND w.professional.id = :professionalId " +
			"AND w.service.id = :serviceId AND w.status = :status " +
			"AND w.preferredDateStart <= :end AND w.preferredDateEnd >= :start")
	boolean existsOverlapping(
			@Param("clientId") Long clientId,
			@Param("professionalId") Long professionalId,
			@Param("serviceId") Long serviceId,
			@Param("status") WaitingListStatus status,
			@Param("start") LocalDate start,
			@Param("end") LocalDate end);

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
	@Query("SELECT w FROM WaitingList w WHERE w.professional.id = :professionalId " +
			"AND w.status = :status AND w.client.id <> :excludedClientId " +
			"AND w.preferredDateStart <= :date AND w.preferredDateEnd >= :date " +
			"ORDER BY w.id ASC")
	List<WaitingList> findWaitingForDate(
			@Param("professionalId") Long professionalId,
			@Param("status") WaitingListStatus status,
			@Param("date") LocalDate date,
			@Param("excludedClientId") Long excludedClientId);
}
