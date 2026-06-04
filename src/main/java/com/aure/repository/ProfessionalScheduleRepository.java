package com.aure.repository;

import com.aure.domain.ProfessionalSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface ProfessionalScheduleRepository extends JpaRepository<ProfessionalSchedule, Long> {

	List<ProfessionalSchedule> findByProfessionalId(Long professionalId);

	Optional<ProfessionalSchedule> findByIdAndProfessionalId(Long id, Long professionalId);

	List<ProfessionalSchedule> findByProfessionalIdAndDayOfWeek(Long professionalId, DayOfWeek dayOfWeek);
}
