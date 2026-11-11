package com.aure.service;

import com.aure.api.dto.MonthlyReportDto;
import com.aure.api.dto.MonthlyReportDto.ServiceSummary;
import com.aure.domain.Appointment;
import com.aure.domain.AppointmentReminder;
import com.aure.domain.AppointmentStatus;
import com.aure.domain.ReminderResponse;
import com.aure.domain.ReminderStatus;
import com.aure.repository.AppointmentReminderRepository;
import com.aure.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProfessionalReportService {

	private static final int TOP_SERVICES = 3;
	private static final Set<AppointmentStatus> OPEN_STATUSES = Set.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);

	private final AppointmentRepository appointmentRepository;
	private final AppointmentReminderRepository appointmentReminderRepository;
	private final ProfessionalService professionalService;
	private final Clock clock;

	@Transactional(readOnly = true)
	public MonthlyReportDto monthly(Long professionalId, YearMonth requestedMonth) {
		professionalService.requireOwnership(professionalId);
		YearMonth month = requestedMonth != null ? requestedMonth : YearMonth.now(clock);
		LocalDate start = month.atDay(1);
		LocalDate end = month.atEndOfMonth();
		LocalDateTime now = LocalDateTime.now(clock);

		List<Appointment> appointments = appointmentRepository.findByProfessionalIdWithFilters(professionalId, start, end, null);
		List<Appointment> completed = withStatus(appointments, AppointmentStatus.COMPLETED);
		List<Appointment> open = appointments.stream().filter(a -> OPEN_STATUSES.contains(a.getStatus())).toList();
		List<Appointment> upcoming = open.stream().filter(a -> startOf(a).isAfter(now)).toList();
		int noShows = withStatus(appointments, AppointmentStatus.NO_SHOW).size();
		int marked = completed.size() + noShows;

		List<AppointmentReminder> reminders = appointmentReminderRepository
				.findByProfessionalAndPeriod(professionalId, ReminderStatus.SENT, start, end);

		return new MonthlyReportDto(
				month.toString(),
				completed.size(),
				sumPrices(completed),
				upcoming.size(),
				sumPrices(upcoming),
				open.size() - upcoming.size(),
				noShows,
				withStatus(appointments, AppointmentStatus.CANCELLED).size(),
				marked == 0 ? null : (double) completed.size() / marked,
				reminders.size(),
				countResponses(reminders, ReminderResponse.CONFIRMED),
				countResponses(reminders, ReminderResponse.CANCELLED),
				topServices(completed));
	}

	private static List<ServiceSummary> topServices(List<Appointment> completed) {
		Map<String, List<Appointment>> byService = completed.stream()
				.collect(Collectors.groupingBy(a -> a.getService().getName()));
		return byService.entrySet().stream()
				.map(entry -> new ServiceSummary(entry.getKey(), entry.getValue().size(), sumPrices(entry.getValue())))
				.sorted(Comparator.comparingInt(ServiceSummary::count).reversed()
						.thenComparing(ServiceSummary::revenue, Comparator.reverseOrder()))
				.limit(TOP_SERVICES)
				.toList();
	}

	private static List<Appointment> withStatus(List<Appointment> appointments, AppointmentStatus status) {
		return appointments.stream().filter(a -> a.getStatus() == status).toList();
	}

	private static BigDecimal sumPrices(List<Appointment> appointments) {
		return appointments.stream().map(Appointment::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private static int countResponses(List<AppointmentReminder> reminders, ReminderResponse response) {
		return (int) reminders.stream().filter(r -> r.getResponse() == response).count();
	}

	private static LocalDateTime startOf(Appointment appointment) {
		return LocalDateTime.of(appointment.getScheduledDate(), appointment.getScheduledTime());
	}
}
