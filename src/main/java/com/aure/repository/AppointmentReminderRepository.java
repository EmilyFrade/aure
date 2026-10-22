package com.aure.repository;

import com.aure.domain.AppointmentReminder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentReminderRepository extends JpaRepository<AppointmentReminder, Long> {
}
