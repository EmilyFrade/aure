package com.aure.repository;

import com.aure.domain.AppointmentReminder;
import com.aure.domain.AppointmentStatus;
import com.aure.domain.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AppointmentReminderRepository extends JpaRepository<AppointmentReminder, Long> {

	@Query("SELECT r FROM AppointmentReminder r JOIN FETCH r.appointment a JOIN FETCH a.client " +
			"JOIN FETCH a.professional p JOIN FETCH p.brand JOIN FETCH a.service " +
			"WHERE a.client.id = :clientId AND r.status = :status AND r.response IS NULL AND a.status IN :statuses " +
			"ORDER BY a.scheduledDate ASC, a.scheduledTime ASC")
	List<AppointmentReminder> findAwaitingResponse(
			@Param("clientId") Long clientId,
			@Param("status") ReminderStatus status,
			@Param("statuses") Collection<AppointmentStatus> statuses);
}
