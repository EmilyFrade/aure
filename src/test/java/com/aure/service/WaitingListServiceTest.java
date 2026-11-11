package com.aure.service;

import com.aure.api.dto.AvailabilityResponseDto;
import com.aure.api.dto.ManualWaitingListRequestDto;
import com.aure.domain.AppointmentStatus;
import com.aure.domain.DayPeriod;
import com.aure.domain.Service;
import com.aure.domain.WaitingList;
import com.aure.domain.WaitingListStatus;
import com.aure.messaging.AppointmentCancelledEvent;
import com.aure.messaging.WhatsAppMessageSender;
import com.aure.repository.AppointmentRepository;
import com.aure.repository.ClientRepository;
import com.aure.repository.ProfessionalRepository;
import com.aure.repository.ServiceRepository;
import com.aure.repository.WaitingListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.aure.service.TestData.CLIENT_PHONE;
import static com.aure.service.TestData.PROFESSIONAL_ID;
import static com.aure.service.TestData.SERVICE_ID;
import static com.aure.service.TestData.TODAY;
import static com.aure.service.TestData.TOMORROW;
import static com.aure.service.TestData.assertStatus;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaitingListServiceTest {

	private static final Long CANCELLED_APPOINTMENT_ID = 1L;
	private static final Long CANCELLING_CLIENT_ID = 99L;

	@Mock WaitingListRepository waitingListRepository;
	@Mock ServiceRepository serviceRepository;
	@Mock ClientRepository clientRepository;
	@Mock ProfessionalRepository professionalRepository;
	@Mock ProfessionalService professionalService;
	@Mock WhatsAppMessageSender whatsAppMessageSender;
	@Mock AvailabilityService availabilityService;
	@Mock AppointmentRepository appointmentRepository;

	WaitingListService waitingListService;

	@BeforeEach
	void setUp() {
		waitingListService = new WaitingListService(waitingListRepository, serviceRepository, clientRepository,
				professionalRepository, professionalService, whatsAppMessageSender, availabilityService, appointmentRepository,
				TestData.CLOCK);
	}

	@Nested
	@DisplayName("Aviso de vaga quando um agendamento é cancelado")
	class NotifyNextInQueue {

		@Test
		@DisplayName("Avisa o primeiro da fila e oferece o horário liberado")
		void notifiesFirstCandidate() {
			cancelledAppointment(60);
			WaitingList first = candidate(20L, TestData.service(60), null);
			WaitingList second = candidate(21L, TestData.service(60), null);
			queue(first, second);
			freeSlots(SERVICE_ID, "10:00", "10:30");

			waitingListService.notifyNextInQueue(cancellation(TOMORROW, LocalTime.of(10, 0)));

			assertThat(first.getStatus()).isEqualTo(WaitingListStatus.NOTIFIED);
			assertThat(first.getOfferedDate()).isEqualTo(TOMORROW);
			assertThat(first.getOfferedTime()).isEqualTo(LocalTime.of(10, 0));
			assertThat(second.getStatus()).isEqualTo(WaitingListStatus.WAITING);
			verify(whatsAppMessageSender).notifyWaitingListSlot(any());
		}

		@Test
		@DisplayName("Não avisa ninguém quando o horário cancelado já passou")
		void ignoresPastSlot() {
			waitingListService.notifyNextInQueue(cancellation(TODAY, LocalTime.of(9, 0)));

			verifyNoInteractions(waitingListRepository, whatsAppMessageSender, availabilityService);
		}

		@Test
		@DisplayName("Pula quem escolheu outro turno")
		void respectsPreferredPeriod() {
			cancelledAppointment(60);
			WaitingList afternoon = candidate(20L, TestData.service(60), DayPeriod.AFTERNOON);
			WaitingList morning = candidate(21L, TestData.service(60), DayPeriod.MORNING);
			queue(afternoon, morning);
			freeSlots(SERVICE_ID, "10:00");

			waitingListService.notifyNextInQueue(cancellation(TOMORROW, LocalTime.of(10, 0)));

			assertThat(afternoon.getStatus()).isEqualTo(WaitingListStatus.WAITING);
			assertThat(morning.getStatus()).isEqualTo(WaitingListStatus.NOTIFIED);
		}

		@Test
		@DisplayName("Pula quem espera um serviço que não cabe na vaga")
		void skipsServiceThatDoesNotFit() {
			cancelledAppointment(60);
			WaitingList longService = candidate(20L, TestData.service(2L, 120), null);
			WaitingList fits = candidate(21L, TestData.service(60), null);
			queue(longService, fits);
			freeSlots(2L);
			freeSlots(SERVICE_ID, "10:00");

			waitingListService.notifyNextInQueue(cancellation(TOMORROW, LocalTime.of(10, 0)));

			assertThat(longService.getStatus()).isEqualTo(WaitingListStatus.WAITING);
			assertThat(fits.getStatus()).isEqualTo(WaitingListStatus.NOTIFIED);
		}

		@Test
		@DisplayName("Pode avisar quem espera outro serviço, se ele couber no horário liberado")
		void offersSlotForAnotherService() {
			cancelledAppointment(60);
			WaitingList otherService = candidate(20L, TestData.service(2L, 30), null);
			queue(otherService);
			freeSlots(2L, "10:30", "14:00");

			waitingListService.notifyNextInQueue(cancellation(TOMORROW, LocalTime.of(10, 0)));

			assertThat(otherService.getOfferedTime()).isEqualTo(LocalTime.of(10, 30));
		}

		@Test
		@DisplayName("Não oferece horário livre fora do intervalo liberado pelo cancelamento")
		void ignoresSlotsOutsideFreedInterval() {
			cancelledAppointment(60);
			WaitingList entry = candidate(20L, TestData.service(60), null);
			queue(entry);
			freeSlots(SERVICE_ID, "14:00");

			waitingListService.notifyNextInQueue(cancellation(TOMORROW, LocalTime.of(10, 0)));

			assertThat(entry.getStatus()).isEqualTo(WaitingListStatus.WAITING);
			verifyNoInteractions(whatsAppMessageSender);
		}

		@Test
		@DisplayName("Ignora entradas de serviços desativados")
		void skipsInactiveService() {
			cancelledAppointment(60);
			Service inactive = TestData.service(60);
			inactive.setActive(false);
			queue(candidate(20L, inactive, null));

			waitingListService.notifyNextInQueue(cancellation(TOMORROW, LocalTime.of(10, 0)));

			verifyNoInteractions(availabilityService, whatsAppMessageSender);
		}
	}

	@Nested
	@DisplayName("Entrada na fila pelo profissional")
	class AddByProfessional {

		@BeforeEach
		void setUp() {
			when(professionalService.getProfessional(PROFESSIONAL_ID)).thenReturn(TestData.professional());
			when(serviceRepository.findByIdAndProfessionalId(SERVICE_ID, PROFESSIONAL_ID)).thenReturn(Optional.of(TestData.service(60)));
			when(clientRepository.findByPhone(CLIENT_PHONE)).thenReturn(Optional.of(TestData.client()));
		}

		@Test
		@DisplayName("Cria a entrada aguardando")
		void createsWaitingEntry() {
			AtomicReference<WaitingList> saved = new AtomicReference<>();
			when(waitingListRepository.save(any(WaitingList.class))).thenAnswer(invocation -> {
				WaitingList entry = invocation.getArgument(0);
				entry.setId(30L);
				saved.set(entry);
				return entry;
			});
			when(waitingListRepository.findByIdWithDetails(30L)).thenAnswer(invocation -> Optional.of(saved.get()));

			waitingListService.addByProfessional(PROFESSIONAL_ID, request(TOMORROW, TOMORROW.plusDays(3)));

			assertThat(saved.get().getStatus()).isEqualTo(WaitingListStatus.WAITING);
			assertThat(saved.get().getPreferredDateEnd()).isEqualTo(TOMORROW.plusDays(3));
		}

		@Test
		@DisplayName("Recusa período que começa no passado")
		void rejectsPastStart() {
			assertStatus(() -> waitingListService.addByProfessional(PROFESSIONAL_ID, request(TODAY.minusDays(1), TOMORROW)),
					HttpStatus.BAD_REQUEST);
		}

		@Test
		@DisplayName("Recusa período de 30 dias ou mais")
		void rejectsRangeTooLong() {
			assertStatus(() -> waitingListService.addByProfessional(PROFESSIONAL_ID, request(TOMORROW, TOMORROW.plusDays(30))),
					HttpStatus.BAD_REQUEST);
		}

		@Test
		@DisplayName("Recusa cliente que já está na fila do mesmo serviço no período")
		void rejectsDuplicate() {
			when(waitingListRepository.existsOverlapping(anyLong(), anyLong(), anyLong(), any(), any(), any())).thenReturn(true);

			assertStatus(() -> waitingListService.addByProfessional(PROFESSIONAL_ID, request(TOMORROW, TOMORROW.plusDays(3))),
					HttpStatus.CONFLICT);
			verify(waitingListRepository, never()).save(any());
		}

		private ManualWaitingListRequestDto request(LocalDate start, LocalDate end) {
			return new ManualWaitingListRequestDto(SERVICE_ID, CLIENT_PHONE, "Ana", start, end, null);
		}
	}

	private void cancelledAppointment(int durationMinutes) {
		when(appointmentRepository.findById(CANCELLED_APPOINTMENT_ID))
				.thenReturn(Optional.of(TestData.appointment(TOMORROW, LocalTime.of(10, 0), durationMinutes, AppointmentStatus.CANCELLED)));
	}

	private void queue(WaitingList... entries) {
		when(waitingListRepository.findWaitingForDate(PROFESSIONAL_ID, WaitingListStatus.WAITING, TOMORROW, CANCELLING_CLIENT_ID))
				.thenReturn(List.of(entries));
		lenient().when(waitingListRepository.save(any(WaitingList.class))).thenAnswer(returnsFirstArg());
	}

	private void freeSlots(Long serviceId, String... times) {
		List<LocalTime> slots = Arrays.stream(times).map(LocalTime::parse).toList();
		when(availabilityService.getAvailableSlots(PROFESSIONAL_ID, TOMORROW, serviceId))
				.thenReturn(new AvailabilityResponseDto(TOMORROW, slots));
	}

	private static WaitingList candidate(Long clientId, Service service, DayPeriod period) {
		return WaitingList.builder()
				.id(clientId)
				.professional(TestData.professional())
				.client(TestData.client(clientId))
				.service(service)
				.preferredDateStart(TOMORROW)
				.preferredDateEnd(TOMORROW)
				.preferredPeriod(period)
				.status(WaitingListStatus.WAITING)
				.build();
	}

	private static AppointmentCancelledEvent cancellation(LocalDate date, LocalTime time) {
		return new AppointmentCancelledEvent(CANCELLED_APPOINTMENT_ID, PROFESSIONAL_ID, CANCELLING_CLIENT_ID, SERVICE_ID, date, time, false);
	}
}
