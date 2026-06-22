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

	@EntityGraph(attributePaths = {"professional", "service"})
	@Query("SELECT a FROM Appointment a WHERE a.id = :id")
	Optional<Appointment> findByIdWithDetails(@Param("id") Long id);

	@EntityGraph(attributePaths = {"professional", "service"})
	List<Appointment> findByClientId(Long clientId);

	@EntityGraph(attributePaths = {"professional", "service"})
	List<Appointment> findByProfessionalId(Long professionalId);

	List<Appointment> findByProfessionalIdAndScheduledDateAndStatusNot(Long professionalId, LocalDate scheduledDate, AppointmentStatus status);

	Optional<Appointment> findByIdAndClientId(Long id, Long clientId);

	Optional<Appointment> findByIdAndProfessionalId(Long id, Long professionalId);
}
