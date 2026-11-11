package com.aure.service;

import com.aure.domain.Appointment;
import com.aure.domain.AppointmentReminder;
import com.aure.domain.AppointmentStatus;
import com.aure.domain.ReminderResponse;
import com.aure.domain.ReminderStatus;
import com.aure.messaging.WhatsAppMessageSender;
import com.aure.repository.AppointmentReminderRepository;
import com.aure.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.aure.service.TestData.CLIENT_ID;
import static com.aure.service.TestData.TODAY;
import static com.aure.service.TestData.TOMORROW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReminderResponseServiceTest {

	private static final String WHATSAPP_NUMBER = "5531999998888";

	@Mock ClientRepository clientRepository;
	@Mock AppointmentReminderRepository appointmentReminderRepository;
	@Mock AppointmentService appointmentService;
	@Mock WhatsAppMessageSender whatsAppMessageSender;

	ReminderResponseService reminderResponseService;

	@BeforeEach
	void setUp() {
		reminderResponseService = new ReminderResponseService(clientRepository, appointmentReminderRepository,
				appointmentService, whatsAppMessageSender, TestData.CLOCK);
	}

	@ParameterizedTest(name = "\"{0}\" confirma")
	@ValueSource(strings = {"1", "Sim", "SIM!", "confirmo", " Confirmar "})
	@DisplayName("Respostas de confirmação confirmam o agendamento e registram a resposta")
	void confirmationAnswers(String text) {
		Appointment appointment = upcomingAppointment();
		AppointmentReminder reminder = pendingReminder(appointment);

		reminderResponseService.handleReply(WHATSAPP_NUMBER, text);

		verify(appointmentService).confirmByReminder(appointment);
		verify(whatsAppMessageSender).notifyReminderConfirmed(any());
		assertThat(reminder.getResponse()).isEqualTo(ReminderResponse.CONFIRMED);
		assertThat(reminder.getRespondedAt()).isNotNull();
	}

	@ParameterizedTest(name = "\"{0}\" cancela")
	@ValueSource(strings = {"2", "Não", "nao", "cancelar", "CANCELA"})
	@DisplayName("Respostas de cancelamento cancelam o agendamento e registram a resposta")
	void cancellationAnswers(String text) {
		Appointment appointment = upcomingAppointment();
		AppointmentReminder reminder = pendingReminder(appointment);

		reminderResponseService.handleReply(WHATSAPP_NUMBER, text);

		verify(appointmentService).cancelByReminder(appointment);
		verify(whatsAppMessageSender).notifyReminderCancelled(any());
		assertThat(reminder.getResponse()).isEqualTo(ReminderResponse.CANCELLED);
	}

	@ParameterizedTest(name = "\"{0}\" é ignorada")
	@ValueSource(strings = {"Ok, obrigada!", "3", "talvez", "Bom dia"})
	@DisplayName("Mensagens que não são resposta ao lembrete são descartadas sem consultar o banco")
	void ignoresOtherMessages(String text) {
		reminderResponseService.handleReply(WHATSAPP_NUMBER, text);

		verifyNoInteractions(clientRepository, appointmentReminderRepository, appointmentService, whatsAppMessageSender);
	}

	@Test
	@DisplayName("Encontra a cliente mesmo quando o WhatsApp envia o número sem o nono dígito")
	void matchesNumberWithoutNinthDigit() {
		reminderResponseService.handleReply("553199998888", "1");

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Collection<String>> candidates = ArgumentCaptor.forClass(Collection.class);
		verify(clientRepository).findByPhoneDigits(candidates.capture());
		assertThat(candidates.getValue()).contains("3199998888", "31999998888");
	}

	@Test
	@DisplayName("Não altera agendamento cujo horário já passou")
	void ignoresPastAppointment() {
		pendingReminder(TestData.appointment(TODAY, LocalTime.of(9, 0), 60, AppointmentStatus.PENDING));

		reminderResponseService.handleReply(WHATSAPP_NUMBER, "2");

		verifyNoInteractions(appointmentService, whatsAppMessageSender);
		verify(appointmentReminderRepository, never()).save(any());
	}

	@Test
	@DisplayName("Sem lembrete aguardando resposta, nada é alterado")
	void noPendingReminder() {
		when(clientRepository.findByPhoneDigits(any())).thenReturn(Optional.of(TestData.client()));

		reminderResponseService.handleReply(WHATSAPP_NUMBER, "1");

		verifyNoInteractions(appointmentService, whatsAppMessageSender);
	}

	private static Appointment upcomingAppointment() {
		return TestData.appointment(TOMORROW, LocalTime.of(10, 0), 60, AppointmentStatus.PENDING);
	}

	private AppointmentReminder pendingReminder(Appointment appointment) {
		AppointmentReminder reminder = AppointmentReminder.builder().appointment(appointment).status(ReminderStatus.SENT).build();
		when(clientRepository.findByPhoneDigits(any())).thenReturn(Optional.of(TestData.client()));
		when(appointmentReminderRepository.findAwaitingResponse(eq(CLIENT_ID), eq(ReminderStatus.SENT), any()))
				.thenReturn(List.of(reminder));
		return reminder;
	}
}
