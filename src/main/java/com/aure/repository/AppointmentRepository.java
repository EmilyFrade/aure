package com.aure.repository;

import com.aure.domain.Appointment;
import com.aure.domain.AppointmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	@EntityGraph(attributePaths = {"professional", "client", "service"})
	@Query("SELECT a FROM Appointment a WHERE a.id = :id")
	Optional<Appointment> findByIdWithDetails(@Param("id") Long id);

	@EntityGraph(attributePaths = {"professional", "client", "service"})
	List<Appointment> findByClientId(Long clientId);

	List<Appointment> findByProfessionalIdAndScheduledDateAndStatusNot(Long professionalId, LocalDate scheduledDate, AppointmentStatus status);

	Optional<Appointment> findByIdAndClientId(Long id, Long clientId);

	Optional<Appointment> findByIdAndProfessionalId(Long id, Long professionalId);

	@EntityGraph(attributePaths = {"professional", "client", "service"})
	@Query("SELECT a FROM Appointment a WHERE a.professional.id = :professionalId " +
			"AND (:startDate IS NULL OR a.scheduledDate >= :startDate) " +
			"AND (:endDate IS NULL OR a.scheduledDate <= :endDate) " +
			"AND (:status IS NULL OR a.status = :status) " +
			"ORDER BY a.scheduledDate ASC, a.scheduledTime ASC")
	List<Appointment> findByProfessionalIdWithFilters(
			@Param("professionalId") Long professionalId,
			@Param("startDate") LocalDate startDate,
			@Param("endDate") LocalDate endDate,
			@Param("status") AppointmentStatus status);
}
