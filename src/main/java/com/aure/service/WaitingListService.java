package com.aure.service;

import com.aure.api.dto.ManualWaitingListRequestDto;
import com.aure.api.dto.WaitingListRequestDto;
import com.aure.api.dto.WaitingListResponseDto;
import com.aure.domain.Client;
import com.aure.domain.DayPeriod;
import com.aure.domain.Professional;
import com.aure.domain.Service;
import com.aure.domain.WaitingList;
import com.aure.domain.WaitingListStatus;
import com.aure.messaging.AppointmentCancelledEvent;
import com.aure.messaging.AppointmentMessage;
import com.aure.messaging.WhatsAppMessageSender;
import com.aure.repository.AppointmentRepository;
import com.aure.repository.ClientRepository;
import com.aure.repository.ProfessionalRepository;
import com.aure.repository.ServiceRepository;
import com.aure.repository.WaitingListRepository;
import com.aure.security.AuthenticatedClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class WaitingListService {

	private static final int BOOKING_WINDOW_DAYS = 60;
	private static final int MAX_RANGE_DAYS = 30;
	private static final Set<WaitingListStatus> ACTIVE_STATUSES = Set.of(WaitingListStatus.WAITING, WaitingListStatus.NOTIFIED);

	private final WaitingListRepository waitingListRepository;
	private final ServiceRepository serviceRepository;
	private final ClientRepository clientRepository;
	private final ProfessionalRepository professionalRepository;
	private final ProfessionalService professionalService;
	private final WhatsAppMessageSender whatsAppMessageSender;
	private final AvailabilityService availabilityService;
	private final AppointmentRepository appointmentRepository;

	@Transactional
	public WaitingListResponseDto join(WaitingListRequestDto request) {
		Client client = currentClient();

		Professional professional = professionalRepository.findById(request.professionalId())
				.filter(Professional::isActive)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profissional não encontrado"));

		Service service = serviceRepository.findByIdAndProfessionalId(request.serviceId(), professional.getId())
				.filter(Service::isActive)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));

		return create(professional, client, service, request.preferredDateStart(), request.preferredDateEnd(), request.preferredPeriod(),
				"Você já está na fila desse serviço nesse período");
	}

	@Transactional
	public WaitingListResponseDto addByProfessional(Long professionalId, ManualWaitingListRequestDto request) {
		professionalService.requireOwnership(professionalId);
		Professional professional = professionalService.getProfessional(professionalId);

		Service service = serviceRepository.findByIdAndProfessionalId(request.serviceId(), professionalId)
				.filter(Service::isActive)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));

		String phone = request.clientPhone().trim();
		String name = request.clientName() == null || request.clientName().isBlank() ? null : request.clientName().trim();
		Client client = clientRepository.findByPhone(phone)
				.orElseGet(() -> clientRepository.save(Client.builder().phone(phone).name(name).build()));
		if (client.getName() == null && name != null) {
			client.setName(name);
		}

		return create(professional, client, service, request.preferredDateStart(), request.preferredDateEnd(), request.preferredPeriod(),
				"Esse cliente já está na fila desse serviço nesse período");
	}

	private WaitingListResponseDto create(Professional professional, Client client, Service service, LocalDate start, LocalDate end,
			DayPeriod period, String duplicateMessage) {
		LocalDate today = LocalDate.now();

		if (start.isAfter(end)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data inicial deve ser anterior ou igual à data final");
		}
		if (start.isBefore(today) || start.isAfter(today.plusDays(BOOKING_WINDOW_DAYS))) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escolha uma data a partir de hoje");
		}
		if (ChronoUnit.DAYS.between(start, end) >= MAX_RANGE_DAYS) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O período da fila pode ter no máximo " + MAX_RANGE_DAYS + " dias");
		}
		if (waitingListRepository.existsOverlapping(client.getId(), professional.getId(), service.getId(), WaitingListStatus.WAITING, start, end)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, duplicateMessage);
		}

		int position = waitingListRepository.countByProfessionalIdAndStatus(professional.getId(), WaitingListStatus.WAITING) + 1;

		WaitingList entry = waitingListRepository.save(WaitingList.builder()
				.professional(professional)
				.client(client)
				.service(service)
				.preferredDateStart(start)
				.preferredDateEnd(end)
				.preferredPeriod(period)
				.position(position)
				.status(WaitingListStatus.WAITING)
				.build());

		return toResponse(waitingListRepository.findByIdWithDetails(entry.getId()).orElseThrow());
	}

	@Transactional(readOnly = true)
	public List<WaitingListResponseDto> listMine() {
		Client client = currentClient();
		return waitingListRepository.findActiveByClient(client.getId(), ACTIVE_STATUSES, LocalDate.now()).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<WaitingListResponseDto> listForProfessional(Long professionalId) {
		professionalService.requireOwnership(professionalId);
		return waitingListRepository.findActiveByProfessional(professionalId, ACTIVE_STATUSES, LocalDate.now()).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public void leave(Long waitingListId) {
		Client client = currentClient();
		WaitingList entry = waitingListRepository.findByIdAndClientId(waitingListId, client.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada na fila não encontrada"));
		close(entry);
	}

	@Transactional
	public void remove(Long professionalId, Long waitingListId) {
		professionalService.requireOwnership(professionalId);
		WaitingList entry = waitingListRepository.findByIdAndProfessionalId(waitingListId, professionalId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada na fila não encontrada"));
		close(entry);
	}

	@Transactional
	public void closeForBooking(Long clientId, Long professionalId, Long serviceId, LocalDate date) {
		waitingListRepository.findOpenForClientOnDate(clientId, professionalId, serviceId, ACTIVE_STATUSES, date)
				.forEach(entry -> entry.setStatus(WaitingListStatus.CANCELLED));
	}

	@Transactional
	public void notifyNextInQueue(AppointmentCancelledEvent event) {
		LocalDate date = event.scheduledDate();
		LocalTime freedStart = event.scheduledTime();
		if (!LocalDateTime.of(date, freedStart).isAfter(LocalDateTime.now())) return;

		LocalTime freedEnd = appointmentRepository.findById(event.appointmentId())
				.map(appointment -> freedStart.plusMinutes(appointment.getDurationMinutes()))
				.orElse(freedStart);

		List<WaitingList> candidates = waitingListRepository.findWaitingForDate(
				event.professionalId(), WaitingListStatus.WAITING, date, event.clientId());

		Map<Long, List<LocalTime>> slotsByService = new HashMap<>();
		for (WaitingList candidate : candidates) {
			Service service = candidate.getService();
			if (!service.isActive()) continue;

			List<LocalTime> slots = slotsByService.computeIfAbsent(service.getId(),
					id -> availabilityService.getAvailableSlots(event.professionalId(), date, id).getSlots());

			Optional<LocalTime> slot = slots.stream()
					.filter(start -> start.isBefore(freedEnd) && start.plusMinutes(service.getDurationMinutes()).isAfter(freedStart))
					.filter(start -> candidate.getPreferredPeriod() == null || candidate.getPreferredPeriod().contains(start))
					.findFirst();
			if (slot.isEmpty()) continue;

			offer(candidate, date, slot.get());
			return;
		}
	}

	private void offer(WaitingList entry, LocalDate date, LocalTime time) {
		entry.setStatus(WaitingListStatus.NOTIFIED);
		entry.setNotifiedAt(Instant.now());
		entry.setOfferedDate(date);
		entry.setOfferedTime(time);
		waitingListRepository.save(entry);

		whatsAppMessageSender.notifyWaitingListSlot(
				AppointmentMessage.of(entry.getClient(), entry.getProfessional(), entry.getService(), date, time));
	}

	private void close(WaitingList entry) {
		if (entry.getStatus() == WaitingListStatus.CANCELLED) return;
		entry.setStatus(WaitingListStatus.CANCELLED);
		waitingListRepository.save(entry);
	}

	private WaitingListResponseDto toResponse(WaitingList entry) {
		int position = entry.getStatus() == WaitingListStatus.WAITING
				? waitingListRepository.countByProfessionalIdAndStatusAndIdLessThan(
						entry.getProfessional().getId(), WaitingListStatus.WAITING, entry.getId()) + 1
				: 0;
		return WaitingListResponseDto.from(entry, position);
	}

	private Client currentClient() {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedClient principal)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não autenticado");
		}

		return clientRepository.findById(principal.clientId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não autenticado"));
	}
}
