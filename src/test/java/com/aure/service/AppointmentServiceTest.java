package com.aure.service;

import com.aure.api.dto.AppointmentRequestDto;
import com.aure.api.dto.AppointmentStatusUpdateDto;
import com.aure.domain.Appointment;
import com.aure.domain.AppointmentStatus;
import com.aure.messaging.AppointmentCancelledEvent;
import com.aure.messaging.AppointmentCreatedEvent;
import com.aure.messaging.AppointmentEventPublisher;
import com.aure.repository.AppointmentRepository;
import com.aure.repository.ClientRepository;
import com.aure.repository.ProfessionalRepository;
import com.aure.repository.ServiceRepository;
import com.aure.security.AuthenticatedClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.aure.service.TestData.CLIENT_ID;
import static com.aure.service.TestData.PROFESSIONAL_ID;
import static com.aure.service.TestData.SERVICE_ID;
import static com.aure.service.TestData.TODAY;
import static com.aure.service.TestData.TOMORROW;
import static com.aure.service.TestData.assertStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

	private static final Long APPOINTMENT_ID = 5L;

	@Mock AppointmentRepository appointmentRepository;
	@Mock ProfessionalRepository professionalRepository;
	@Mock ServiceRepository serviceRepository;
	@Mock ClientRepository clientRepository;
	@Mock ProfessionalService professionalService;
	@Mock AppointmentEventPublisher eventPublisher;
	@Mock WaitingListService waitingListService;

	AppointmentService appointmentService;
	Appointment saved;

	@BeforeEach
	void setUp() {
		appointmentService = new AppointmentService(appointmentRepository, professionalRepository, serviceRepository,
				clientRepository, professionalService, eventPublisher, waitingListService, TestData.CLOCK);

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
				new AuthenticatedClient(CLIENT_ID, UUID.randomUUID()), null, List.of()));
		lenient().when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(TestData.client()));
	}

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("Cria o agendamento como pendente, com duração e preço do serviço, e publica o evento")
	void createsPendingAppointment() {
		bookableService(60);
		savesAppointment();

		appointmentService.createAppointment(request(LocalTime.of(10, 0)));

		assertThat(saved.getStatus()).isEqualTo(AppointmentStatus.PENDING);
		assertThat(saved.getDurationMinutes()).isEqualTo(60);
		assertThat(saved.getPrice()).isEqualByComparingTo("50.00");
		verify(waitingListService).closeForBooking(CLIENT_ID, PROFESSIONAL_ID, SERVICE_ID, TOMORROW);
		verify(eventPublisher).publish(any(AppointmentCreatedEvent.class));
	}

	@Test
	@DisplayName("Recusa horário que se sobrepõe a outro agendamento do profissional")
	void rejectsProfessionalConflict() {
		bookableService(60);
		when(appointmentRepository.findByProfessionalIdAndScheduledDateAndStatusNot(PROFESSIONAL_ID, TOMORROW, AppointmentStatus.CANCELLED))
				.thenReturn(List.of(TestData.appointment(TOMORROW, LocalTime.of(10, 30), 60, AppointmentStatus.CONFIRMED)));

		assertThatThrownBy(() -> appointmentService.createAppointment(request(LocalTime.of(10, 0))))
				.hasMessageContaining("Horário indisponível");
		verify(appointmentRepository, never()).save(any());
	}

	@Test
	@DisplayName("Recusa horário em que a cliente já tem agendamento, mesmo com outro profissional")
	void rejectsClientConflict() {
		bookableService(60);
		when(appointmentRepository.findByClientIdAndScheduledDateAndStatusNot(CLIENT_ID, TOMORROW, AppointmentStatus.CANCELLED))
				.thenReturn(List.of(TestData.appointment(TOMORROW, LocalTime.of(10, 0), 30, AppointmentStatus.PENDING)));

		assertThatThrownBy(() -> appointmentService.createAppointment(request(LocalTime.of(10, 0))))
				.hasMessageContaining("Você já tem um agendamento nesse horário");
		verify(appointmentRepository, never()).save(any());
	}

	@Test
	@DisplayName("Aceita horário que começa exatamente quando o anterior termina")
	void acceptsBackToBackAppointment() {
		bookableService(60);
		savesAppointment();
		when(appointmentRepository.findByProfessionalIdAndScheduledDateAndStatusNot(PROFESSIONAL_ID, TOMORROW, AppointmentStatus.CANCELLED))
				.thenReturn(List.of(TestData.appointment(TOMORROW, LocalTime.of(9, 0), 60, AppointmentStatus.CONFIRMED)));

		appointmentService.createAppointment(request(LocalTime.of(10, 0)));

		assertThat(saved.getScheduledTime()).isEqualTo(LocalTime.of(10, 0));
	}

	@Test
	@DisplayName("Cliente não cancela horário que já passou")
	void clientCannotCancelPastAppointment() {
		when(appointmentRepository.findByIdAndClientId(APPOINTMENT_ID, CLIENT_ID))
				.thenReturn(Optional.of(TestData.appointment(TODAY, LocalTime.of(9, 0), 60, AppointmentStatus.CONFIRMED)));

		assertStatus(() -> appointmentService.cancelByClient(APPOINTMENT_ID), HttpStatus.CONFLICT);
		verify(eventPublisher, never()).publish(any(AppointmentCancelledEvent.class));
	}

	@Test
	@DisplayName("Cancelamento pela cliente publica o evento sem aviso de cancelamento pelo profissional")
	void clientCancelsUpcomingAppointment() {
		Appointment appointment = TestData.appointment(TOMORROW, LocalTime.of(10, 0), 60, AppointmentStatus.PENDING);
		when(appointmentRepository.findByIdAndClientId(APPOINTMENT_ID, CLIENT_ID)).thenReturn(Optional.of(appointment));
		when(appointmentRepository.save(any(Appointment.class))).thenAnswer(returnsFirstArg());

		appointmentService.cancelByClient(APPOINTMENT_ID);

		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
		assertThat(appointment.getCancelledAt()).isNotNull();
		assertThat(publishedCancellation().byProfessional()).isFalse();
	}

	@Test
	@DisplayName("Cancelamento pelo profissional publica o evento marcado como do profissional")
	void professionalCancelsAppointment() {
		Appointment appointment = TestData.appointment(TOMORROW, LocalTime.of(10, 0), 60, AppointmentStatus.CONFIRMED);
		when(appointmentRepository.findByIdAndProfessionalId(APPOINTMENT_ID, PROFESSIONAL_ID)).thenReturn(Optional.of(appointment));
		when(appointmentRepository.save(any(Appointment.class))).thenAnswer(returnsFirstArg());

		appointmentService.cancelByProfessional(PROFESSIONAL_ID, APPOINTMENT_ID);

		assertThat(publishedCancellation().byProfessional()).isTrue();
	}

	@Test
	@DisplayName("Agendamento concluído não pode ser cancelado")
	void cannotCancelCompletedAppointment() {
		when(appointmentRepository.findByIdAndProfessionalId(APPOINTMENT_ID, PROFESSIONAL_ID))
				.thenReturn(Optional.of(TestData.appointment(TOMORROW, LocalTime.of(10, 0), 60, AppointmentStatus.COMPLETED)));

		assertStatus(() -> appointmentService.cancelByProfessional(PROFESSIONAL_ID, APPOINTMENT_ID), HttpStatus.CONFLICT);
	}

	@Test
	@DisplayName("Mudança de status não aceita cancelar (o cancelamento tem rota própria)")
	void statusUpdateRejectsCancelled() {
		assertStatus(() -> appointmentService.updateStatus(PROFESSIONAL_ID, APPOINTMENT_ID,
				new AppointmentStatusUpdateDto(AppointmentStatus.CANCELLED)), HttpStatus.BAD_REQUEST);
	}

	@Test
	@DisplayName("Agendamento em estado final não muda de status")
	void statusUpdateRejectsTerminalAppointment() {
		when(appointmentRepository.findByIdAndProfessionalId(APPOINTMENT_ID, PROFESSIONAL_ID))
				.thenReturn(Optional.of(TestData.appointment(TODAY, LocalTime.of(9, 0), 60, AppointmentStatus.NO_SHOW)));

		assertStatus(() -> appointmentService.updateStatus(PROFESSIONAL_ID, APPOINTMENT_ID,
				new AppointmentStatusUpdateDto(AppointmentStatus.COMPLETED)), HttpStatus.CONFLICT);
	}

	@Test
	@DisplayName("Resposta 1 ao lembrete confirma o agendamento pendente")
	void reminderConfirmsPendingAppointment() {
		Appointment appointment = TestData.appointment(TOMORROW, LocalTime.of(10, 0), 60, AppointmentStatus.PENDING);

		appointmentService.confirmByReminder(appointment);

		assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
		assertThat(appointment.getConfirmedAt()).isNotNull();
		verify(appointmentRepository).save(appointment);
	}

	@Test
	@DisplayName("Resposta 1 ao lembrete não altera agendamento já confirmado")
	void reminderIgnoresConfirmedAppointment() {
		Appointment appointment = TestData.appointment(TOMORROW, LocalTime.of(10, 0), 60, AppointmentStatus.CONFIRMED);

		appointmentService.confirmByReminder(appointment);

		verify(appointmentRepository, never()).save(any());
	}

	private void bookableService(int durationMinutes) {
		when(professionalRepository.findById(PROFESSIONAL_ID)).thenReturn(Optional.of(TestData.professional()));
		when(serviceRepository.findByIdAndProfessionalId(SERVICE_ID, PROFESSIONAL_ID))
				.thenReturn(Optional.of(TestData.service(durationMinutes)));
	}

	private void savesAppointment() {
		when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
			saved = invocation.getArgument(0);
			saved.setId(100L);
			return saved;
		});
		when(appointmentRepository.findByIdWithDetails(100L)).thenAnswer(invocation -> Optional.of(saved));
	}

	private AppointmentCancelledEvent publishedCancellation() {
		ArgumentCaptor<AppointmentCancelledEvent> event = ArgumentCaptor.forClass(AppointmentCancelledEvent.class);
		verify(eventPublisher).publish(event.capture());
		return event.getValue();
	}

	private static AppointmentRequestDto request(LocalTime time) {
		return new AppointmentRequestDto(PROFESSIONAL_ID, SERVICE_ID, TOMORROW, time, null);
	}
}
