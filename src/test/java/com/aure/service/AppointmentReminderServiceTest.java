package com.aure.service;

import com.aure.domain.Appointment;
import com.aure.domain.AppointmentReminder;
import com.aure.domain.AppointmentStatus;
import com.aure.domain.ReminderStatus;
import com.aure.messaging.WhatsAppMessageSender;
import com.aure.repository.AppointmentReminderRepository;
import com.aure.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static com.aure.service.TestData.NOW;
import static com.aure.service.TestData.TODAY;
import static com.aure.service.TestData.TOMORROW;
import static com.aure.service.TestData.at;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentReminderServiceTest {

	@Mock AppointmentRepository appointmentRepository;
	@Mock AppointmentReminderRepository appointmentReminderRepository;
	@Mock WhatsAppMessageSender whatsAppMessageSender;

	AppointmentReminderService appointmentReminderService;

	@BeforeEach
	void setUp() {
		appointmentReminderService = new AppointmentReminderService(appointmentRepository, appointmentReminderRepository,
				whatsAppMessageSender, TestData.CLOCK);
	}

	@Test
	@DisplayName("Envia o lembrete de agendamento das próximas 24 horas e registra o envio")
	void sendsReminderWithinWindow() {
		due(TestData.appointment(TOMORROW, LocalTime.of(9, 0), 60, AppointmentStatus.CONFIRMED));
		when(whatsAppMessageSender.sendAppointmentReminder(any())).thenReturn(true);

		appointmentReminderService.sendDueReminders();

		assertThat(savedReminder().getStatus()).isEqualTo(ReminderStatus.SENT);
	}

	@Test
	@DisplayName("Registra a falha quando o WhatsApp não envia, para tentar de novo na rodada seguinte")
	void recordsFailure() {
		due(TestData.appointment(TOMORROW, LocalTime.of(9, 0), 60, AppointmentStatus.PENDING));
		when(whatsAppMessageSender.sendAppointmentReminder(any())).thenReturn(false);

		appointmentReminderService.sendDueReminders();

		assertThat(savedReminder().getStatus()).isEqualTo(ReminderStatus.FAILED);
	}

	@Test
	@DisplayName("Reaproveita o registro de uma tentativa que falhou antes")
	void retriesFailedReminder() {
		Appointment appointment = TestData.appointment(TOMORROW, LocalTime.of(9, 0), 60, AppointmentStatus.PENDING);
		AppointmentReminder failed = AppointmentReminder.builder().id(7L).appointment(appointment).status(ReminderStatus.FAILED).build();
		due(appointment);
		when(appointmentReminderRepository.findByAppointmentId(appointment.getId())).thenReturn(Optional.of(failed));
		when(whatsAppMessageSender.sendAppointmentReminder(any())).thenReturn(true);

		appointmentReminderService.sendDueReminders();

		assertThat(savedReminder()).isSameAs(failed);
		assertThat(failed.getStatus()).isEqualTo(ReminderStatus.SENT);
	}

	@Test
	@DisplayName("Ainda não envia para agendamento a mais de 24 horas")
	void skipsAppointmentOutsideWindow() {
		due(TestData.appointment(TOMORROW, LocalTime.of(11, 0), 60, AppointmentStatus.CONFIRMED));

		appointmentReminderService.sendDueReminders();

		verifyNoInteractions(whatsAppMessageSender);
		verify(appointmentReminderRepository, never()).save(any());
	}

	@Test
	@DisplayName("Não envia lembrete para agendamento feito com menos de 24 horas de antecedência")
	void skipsAppointmentBookedWithinLeadTime() {
		Appointment appointment = TestData.appointment(TOMORROW, LocalTime.of(9, 0), 60, AppointmentStatus.PENDING);
		appointment.setCreatedAt(at(NOW.minusMinutes(30)));
		due(appointment);

		appointmentReminderService.sendDueReminders();

		verifyNoInteractions(whatsAppMessageSender);
	}

	@Test
	@DisplayName("Não envia lembrete para horário que já passou")
	void skipsPastAppointment() {
		due(TestData.appointment(TODAY, LocalTime.of(9, 0), 60, AppointmentStatus.CONFIRMED));

		appointmentReminderService.sendDueReminders();

		verifyNoInteractions(whatsAppMessageSender);
	}

	private void due(Appointment appointment) {
		when(appointmentRepository.findWithoutSentReminder(any(), eq(TODAY), eq(TOMORROW))).thenReturn(List.of(appointment));
	}

	private AppointmentReminder savedReminder() {
		ArgumentCaptor<AppointmentReminder> reminder = ArgumentCaptor.forClass(AppointmentReminder.class);
		verify(appointmentReminderRepository).save(reminder.capture());
		return reminder.getValue();
	}
}
