package com.aure.service;

import com.aure.api.dto.BlockRequestDto;
import com.aure.api.dto.ScheduleRequestDto;
import com.aure.domain.Appointment;
import com.aure.domain.AppointmentStatus;
import com.aure.domain.ProfessionalBlock;
import com.aure.domain.ProfessionalSchedule;
import com.aure.repository.AppointmentRepository;
import com.aure.repository.ProfessionalBlockRepository;
import com.aure.repository.ProfessionalScheduleRepository;
import com.aure.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.aure.service.TestData.PROFESSIONAL_ID;
import static com.aure.service.TestData.SERVICE_ID;
import static com.aure.service.TestData.TODAY;
import static com.aure.service.TestData.TOMORROW;
import static com.aure.service.TestData.assertStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

	@Mock ProfessionalScheduleRepository scheduleRepository;
	@Mock ProfessionalBlockRepository blockRepository;
	@Mock AppointmentRepository appointmentRepository;
	@Mock ProfessionalService professionalService;
	@Mock ServiceRepository serviceRepository;

	AvailabilityService availabilityService;

	@BeforeEach
	void setUp() {
		availabilityService = new AvailabilityService(scheduleRepository, blockRepository, appointmentRepository,
				professionalService, serviceRepository, TestData.CLOCK);
	}

	@Test
	@DisplayName("Gera horários a cada 30 minutos que cabem inteiros no expediente")
	void slotsEveryThirtyMinutes() {
		serviceOf(60);
		workingHours(TOMORROW, "09:00-12:00");

		assertThat(slots(TOMORROW)).containsExactlyElementsOf(times("09:00", "09:30", "10:00", "10:30", "11:00"));
	}

	@Test
	@DisplayName("Respeita o intervalo entre dois períodos de atendimento no mesmo dia")
	void respectsLunchBreak() {
		serviceOf(60);
		workingHours(TOMORROW, "09:00-12:00", "13:00-15:00");

		assertThat(slots(TOMORROW))
				.containsExactlyElementsOf(times("09:00", "09:30", "10:00", "10:30", "11:00", "13:00", "13:30", "14:00"));
	}

	@Test
	@DisplayName("Dia sem horário de atendimento não tem horários livres")
	void noWorkingHours() {
		serviceOf(60);

		assertThat(slots(TOMORROW)).isEmpty();
	}

	@Test
	@DisplayName("Não oferece horários de hoje que já passaram")
	void skipsPastSlotsToday() {
		serviceOf(60);
		workingHours(TODAY, "09:00-12:00");

		assertThat(slots(TODAY)).containsExactlyElementsOf(times("10:30", "11:00"));
	}

	@Test
	@DisplayName("Remove os horários que se sobrepõem a um agendamento existente")
	void skipsSlotsOverlappingAppointment() {
		serviceOf(60);
		workingHours(TOMORROW, "09:00-12:00");
		appointments(TOMORROW, TestData.appointment(TOMORROW, LocalTime.of(10, 0), 60, AppointmentStatus.CONFIRMED));

		assertThat(slots(TOMORROW)).containsExactlyElementsOf(times("09:00", "11:00"));
	}

	@Test
	@DisplayName("Remove os horários que caem em um bloqueio da agenda")
	void skipsBlockedSlots() {
		serviceOf(60);
		workingHours(TOMORROW, "09:00-12:00");
		when(blockRepository.findByProfessionalIdAndStartDatetimeLessThanAndEndDatetimeGreaterThan(eq(PROFESSIONAL_ID), any(), any()))
				.thenReturn(List.of(ProfessionalBlock.builder()
						.startDatetime(TOMORROW.atTime(9, 0))
						.endDatetime(TOMORROW.atTime(10, 30))
						.build()));

		assertThat(slots(TOMORROW)).containsExactlyElementsOf(times("10:30", "11:00"));
	}

	@Test
	@DisplayName("Serviço de outro profissional ou inexistente retorna 404")
	void unknownService() {
		assertStatus(() -> slots(TOMORROW), HttpStatus.NOT_FOUND);
	}

	@Test
	@DisplayName("Não aceita horário de atendimento sobreposto a outro do mesmo dia")
	void rejectsOverlappingSchedule() {
		when(scheduleRepository.findByProfessionalIdAndDayOfWeek(PROFESSIONAL_ID, DayOfWeek.MONDAY))
				.thenReturn(List.of(schedule(5L, DayOfWeek.MONDAY, "09:00-12:00")));

		assertStatus(() -> availabilityService.createSchedule(PROFESSIONAL_ID,
				new ScheduleRequestDto(DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(14, 0))), HttpStatus.CONFLICT);
		verify(scheduleRepository, never()).save(any());
	}

	@Test
	@DisplayName("Não aceita horário de atendimento com início depois do fim")
	void rejectsInvertedSchedule() {
		assertStatus(() -> availabilityService.createSchedule(PROFESSIONAL_ID,
				new ScheduleRequestDto(DayOfWeek.MONDAY, LocalTime.of(14, 0), LocalTime.of(9, 0))), HttpStatus.BAD_REQUEST);
	}

	@Test
	@DisplayName("Não aceita bloqueio sobre um agendamento existente")
	void rejectsBlockOverAppointment() {
		appointments(TOMORROW, TestData.appointment(TOMORROW, LocalTime.of(10, 0), 60, AppointmentStatus.PENDING));

		assertStatus(() -> availabilityService.createBlock(PROFESSIONAL_ID,
				new BlockRequestDto(TOMORROW.atTime(9, 0), TOMORROW.atTime(12, 0), "Médico")), HttpStatus.CONFLICT);
		verify(blockRepository, never()).save(any());
	}

	private void serviceOf(int durationMinutes) {
		when(serviceRepository.findByIdAndProfessionalId(SERVICE_ID, PROFESSIONAL_ID))
				.thenReturn(Optional.of(TestData.service(durationMinutes)));
	}

	private void workingHours(LocalDate date, String... ranges) {
		List<ProfessionalSchedule> schedules = Arrays.stream(ranges)
				.map(range -> schedule(null, date.getDayOfWeek(), range))
				.toList();
		when(scheduleRepository.findByProfessionalIdAndDayOfWeek(PROFESSIONAL_ID, date.getDayOfWeek())).thenReturn(schedules);
	}

	private void appointments(LocalDate date, Appointment... appointments) {
		when(appointmentRepository.findByProfessionalIdAndScheduledDateAndStatusNot(PROFESSIONAL_ID, date, AppointmentStatus.CANCELLED))
				.thenReturn(List.of(appointments));
	}

	private List<LocalTime> slots(LocalDate date) {
		return availabilityService.getAvailableSlots(PROFESSIONAL_ID, date, SERVICE_ID).getSlots();
	}

	private static ProfessionalSchedule schedule(Long id, DayOfWeek day, String range) {
		String[] parts = range.split("-");
		return ProfessionalSchedule.builder()
				.id(id)
				.dayOfWeek(day)
				.startTime(LocalTime.parse(parts[0]))
				.endTime(LocalTime.parse(parts[1]))
				.build();
	}

	private static List<LocalTime> times(String... values) {
		return Arrays.stream(values).map(LocalTime::parse).toList();
	}
}
