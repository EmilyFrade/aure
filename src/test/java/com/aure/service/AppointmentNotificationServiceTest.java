package com.aure.service;

import com.aure.domain.Appointment;
import com.aure.domain.AppointmentStatus;
import com.aure.messaging.AppointmentCancelledEvent;
import com.aure.messaging.AppointmentCreatedEvent;
import com.aure.messaging.WhatsAppMessageSender;
import com.aure.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static com.aure.service.TestData.CLIENT_ID;
import static com.aure.service.TestData.PROFESSIONAL_ID;
import static com.aure.service.TestData.SERVICE_ID;
import static com.aure.service.TestData.TODAY;
import static com.aure.service.TestData.TOMORROW;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentNotificationServiceTest {

	private static final Long APPOINTMENT_ID = 5L;

	@Mock AppointmentRepository appointmentRepository;
	@Mock WhatsAppMessageSender whatsAppMessageSender;

	AppointmentNotificationService appointmentNotificationService;

	@BeforeEach
	void setUp() {
		appointmentNotificationService = new AppointmentNotificationService(appointmentRepository, whatsAppMessageSender, TestData.CLOCK);
	}

	@Test
	@DisplayName("Agendamento manual gera a mensagem de horário agendado pelo profissional")
	void notifiesManualBooking() {
		stored(TestData.appointment(TOMORROW, LocalTime.of(10, 0), 60, AppointmentStatus.CONFIRMED));

		appointmentNotificationService.notifyCreated(created(TOMORROW, true));

		verify(whatsAppMessageSender).notifyAppointmentCreated(any(), eq(true));
	}

	@Test
	@DisplayName("Agendamento cancelado antes do envio não gera confirmação")
	void skipsCancelledBeforeSending() {
		stored(TestData.appointment(TOMORROW, LocalTime.of(10, 0), 60, AppointmentStatus.CANCELLED));

		appointmentNotificationService.notifyCreated(created(TOMORROW, false));

		verify(whatsAppMessageSender, never()).notifyAppointmentCreated(any(), anyBoolean());
	}

	@Test
	@DisplayName("Cancelamento pela própria cliente não gera aviso")
	void skipsCancellationByClient() {
		appointmentNotificationService.notifyCancelled(cancelled(TOMORROW, false));

		verifyNoInteractions(appointmentRepository, whatsAppMessageSender);
	}

	@Test
	@DisplayName("Cancelamento pelo profissional avisa a cliente")
	void notifiesCancellationByProfessional() {
		stored(TestData.appointment(TOMORROW, LocalTime.of(10, 0), 60, AppointmentStatus.CANCELLED));

		appointmentNotificationService.notifyCancelled(cancelled(TOMORROW, true));

		verify(whatsAppMessageSender).notifyAppointmentCancelled(any());
	}

	@Test
	@DisplayName("Cancelamento de horário que já passou não gera aviso")
	void skipsPastCancellation() {
		stored(TestData.appointment(TODAY, LocalTime.of(9, 0), 60, AppointmentStatus.CANCELLED));

		appointmentNotificationService.notifyCancelled(cancelled(TODAY, true));

		verifyNoInteractions(whatsAppMessageSender);
	}

	private void stored(Appointment appointment) {
		when(appointmentRepository.findByIdWithDetails(APPOINTMENT_ID)).thenReturn(Optional.of(appointment));
	}

	private static AppointmentCreatedEvent created(LocalDate date, boolean byProfessional) {
		return new AppointmentCreatedEvent(APPOINTMENT_ID, PROFESSIONAL_ID, CLIENT_ID, SERVICE_ID, date, LocalTime.of(10, 0), byProfessional);
	}

	private static AppointmentCancelledEvent cancelled(LocalDate date, boolean byProfessional) {
		return new AppointmentCancelledEvent(APPOINTMENT_ID, PROFESSIONAL_ID, CLIENT_ID, SERVICE_ID, date, LocalTime.of(10, 0), byProfessional);
	}
}
