package com.aure.service;

import com.aure.api.dto.MonthlyReportDto;
import com.aure.domain.Appointment;
import com.aure.domain.AppointmentReminder;
import com.aure.domain.AppointmentStatus;
import com.aure.domain.ReminderResponse;
import com.aure.domain.ReminderStatus;
import com.aure.repository.AppointmentReminderRepository;
import com.aure.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

import static com.aure.service.TestData.PROFESSIONAL_ID;
import static com.aure.service.TestData.TODAY;
import static com.aure.service.TestData.TOMORROW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessionalReportServiceTest {

	private static final YearMonth NOVEMBER = YearMonth.of(2026, 11);
	private static final LocalDate FIRST_DAY = NOVEMBER.atDay(1);
	private static final LocalDate LAST_DAY = NOVEMBER.atEndOfMonth();

	@Mock AppointmentRepository appointmentRepository;
	@Mock AppointmentReminderRepository appointmentReminderRepository;
	@Mock ProfessionalService professionalService;

	ProfessionalReportService professionalReportService;

	@BeforeEach
	void setUp() {
		professionalReportService = new ProfessionalReportService(appointmentRepository, appointmentReminderRepository,
				professionalService, TestData.CLOCK);
	}

	@Test
	@DisplayName("Soma o faturamento só dos atendimentos concluídos")
	void revenueFromCompletedOnly() {
		appointments(
				appointment(TODAY.minusDays(3), "Corte", "60.00", AppointmentStatus.COMPLETED),
				appointment(TODAY.minusDays(2), "Escova", "40.00", AppointmentStatus.COMPLETED),
				appointment(TODAY.minusDays(1), "Corte", "60.00", AppointmentStatus.CANCELLED),
				appointment(TODAY.minusDays(1), "Corte", "60.00", AppointmentStatus.NO_SHOW));

		MonthlyReportDto report = report();

		assertThat(report.completedCount()).isEqualTo(2);
		assertThat(report.revenue()).isEqualByComparingTo("100.00");
		assertThat(report.cancelledCount()).isEqualTo(1);
		assertThat(report.noShowCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("Separa os agendamentos futuros (previsto) dos passados ainda sem marcação")
	void separatesUpcomingFromUnmarked() {
		appointments(
				appointment(TOMORROW, "Corte", "60.00", AppointmentStatus.CONFIRMED),
				appointment(TOMORROW.plusDays(1), "Escova", "40.00", AppointmentStatus.PENDING),
				appointment(TODAY.minusDays(1), "Corte", "60.00", AppointmentStatus.CONFIRMED));

		MonthlyReportDto report = report();

		assertThat(report.upcomingCount()).isEqualTo(2);
		assertThat(report.expectedRevenue()).isEqualByComparingTo("100.00");
		assertThat(report.unmarkedCount()).isEqualTo(1);
	}

	@Test
	@DisplayName("Calcula o comparecimento entre os atendimentos concluídos e as faltas")
	void attendanceRate() {
		appointments(
				appointment(TODAY.minusDays(4), "Corte", "60.00", AppointmentStatus.COMPLETED),
				appointment(TODAY.minusDays(3), "Corte", "60.00", AppointmentStatus.COMPLETED),
				appointment(TODAY.minusDays(2), "Corte", "60.00", AppointmentStatus.COMPLETED),
				appointment(TODAY.minusDays(1), "Corte", "60.00", AppointmentStatus.NO_SHOW));

		assertThat(report().attendanceRate()).isEqualTo(0.75);
	}

	@Test
	@DisplayName("Sem atendimentos marcados, o comparecimento fica vazio em vez de zero")
	void attendanceRateWithoutMarkedAppointments() {
		appointments(appointment(TOMORROW, "Corte", "60.00", AppointmentStatus.PENDING));

		assertThat(report().attendanceRate()).isNull();
	}

	@Test
	@DisplayName("Conta os lembretes enviados e as respostas 1 e 2")
	void countsReminderResponses() {
		Appointment appointment = appointment(TOMORROW, "Corte", "60.00", AppointmentStatus.CONFIRMED);
		appointments(appointment);
		when(appointmentReminderRepository.findByProfessionalAndPeriod(PROFESSIONAL_ID, ReminderStatus.SENT, FIRST_DAY, LAST_DAY))
				.thenReturn(List.of(
						reminder(appointment, ReminderResponse.CONFIRMED),
						reminder(appointment, ReminderResponse.CONFIRMED),
						reminder(appointment, ReminderResponse.CANCELLED),
						reminder(appointment, null)));

		MonthlyReportDto report = report();

		assertThat(report.remindersSent()).isEqualTo(4);
		assertThat(report.remindersConfirmed()).isEqualTo(2);
		assertThat(report.remindersCancelled()).isEqualTo(1);
	}

	@Test
	@DisplayName("Lista os três serviços mais feitos, do mais frequente para o menos")
	void topServices() {
		appointments(
				appointment(TODAY.minusDays(5), "Corte", "60.00", AppointmentStatus.COMPLETED),
				appointment(TODAY.minusDays(4), "Corte", "60.00", AppointmentStatus.COMPLETED),
				appointment(TODAY.minusDays(4), "Escova", "40.00", AppointmentStatus.COMPLETED),
				appointment(TODAY.minusDays(3), "Hidratação", "80.00", AppointmentStatus.COMPLETED),
				appointment(TODAY.minusDays(2), "Manicure", "30.00", AppointmentStatus.COMPLETED),
				appointment(TODAY.minusDays(1), "Manicure", "30.00", AppointmentStatus.CANCELLED));

		List<MonthlyReportDto.ServiceSummary> top = report().topServices();

		assertThat(top).extracting(MonthlyReportDto.ServiceSummary::name).containsExactly("Corte", "Hidratação", "Escova");
		assertThat(top.getFirst().revenue()).isEqualByComparingTo("120.00");
	}

	@Test
	@DisplayName("Sem mês informado, usa o mês atual")
	void defaultsToCurrentMonth() {
		MonthlyReportDto report = professionalReportService.monthly(PROFESSIONAL_ID, null);

		assertThat(report.month()).isEqualTo("2026-11");
		assertThat(report.completedCount()).isZero();
		assertThat(report.revenue()).isEqualByComparingTo("0");
	}

	private MonthlyReportDto report() {
		return professionalReportService.monthly(PROFESSIONAL_ID, NOVEMBER);
	}

	private void appointments(Appointment... appointments) {
		when(appointmentRepository.findByProfessionalIdWithFilters(eq(PROFESSIONAL_ID), eq(FIRST_DAY), eq(LAST_DAY), isNull()))
				.thenReturn(Arrays.asList(appointments));
	}

	private static Appointment appointment(LocalDate date, String serviceName, String price, AppointmentStatus status) {
		Appointment appointment = TestData.appointment(date, LocalTime.of(10, 0), 60, status);
		appointment.getService().setName(serviceName);
		appointment.setPrice(new BigDecimal(price));
		return appointment;
	}

	private static AppointmentReminder reminder(Appointment appointment, ReminderResponse response) {
		return AppointmentReminder.builder().appointment(appointment).status(ReminderStatus.SENT).response(response).build();
	}
}
