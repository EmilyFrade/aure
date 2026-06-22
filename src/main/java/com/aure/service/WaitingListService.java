package com.aure.service;

import com.aure.api.dto.WaitingListRequestDto;
import com.aure.api.dto.WaitingListResponseDto;
import com.aure.domain.Client;
import com.aure.domain.Professional;
import com.aure.domain.Service;
import com.aure.domain.WaitingList;
import com.aure.domain.WaitingListStatus;
import com.aure.messaging.AppointmentCancelledEvent;
import com.aure.messaging.WhatsAppMessageSender;
import com.aure.repository.ClientRepository;
import com.aure.repository.ServiceRepository;
import com.aure.repository.WaitingListRepository;
import com.aure.security.AuthenticatedClient;
import com.aure.security.AuthenticatedProfessional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WaitingListService {

	private final WaitingListRepository waitingListRepository;
	private final ServiceRepository serviceRepository;
	private final ClientRepository clientRepository;
	private final ProfessionalService professionalService;
	private final WhatsAppMessageSender whatsAppMessageSender;

	@Transactional
	public WaitingListResponseDto join(Long professionalId, WaitingListRequestDto request) {
		Client client = currentClient();
		Professional professional = professionalService.getProfessional(professionalId);

		Service service = serviceRepository.findByIdAndProfessionalId(request.serviceId(), professionalId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));

		if (request.preferredDateStart().isAfter(request.preferredDateEnd())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data inicial deve ser anterior ou igual à data final");
		}

		int position = waitingListRepository.countByProfessionalIdAndServiceIdAndStatus(professionalId, service.getId(), WaitingListStatus.WAITING) + 1;

		WaitingList entry = WaitingList.builder()
				.professional(professional)
				.client(client)
				.service(service)
				.preferredDateStart(request.preferredDateStart())
				.preferredDateEnd(request.preferredDateEnd())
				.position(position)
				.status(WaitingListStatus.WAITING)
				.build();

		return WaitingListResponseDto.from(waitingListRepository.save(entry));
	}

	@Transactional(readOnly = true)
	public List<WaitingListResponseDto> listForProfessional(Long professionalId) {
		professionalService.requireOwnership(professionalId);
		return waitingListRepository.findByProfessionalIdOrderByPositionAsc(professionalId).stream().map(WaitingListResponseDto::from).toList();
	}

	@Transactional
	public void leave(Long professionalId, Long waitingListId) {
		WaitingList entry = getAccessibleEntry(professionalId, waitingListId);
		entry.setStatus(WaitingListStatus.CANCELLED);
		waitingListRepository.save(entry);
	}

	@Transactional
	public void notifyNextInQueue(AppointmentCancelledEvent event) {
		List<WaitingList> candidates = waitingListRepository.findMatchingWaitingEntries(event.professionalId(), event.serviceId(), WaitingListStatus.WAITING, event.scheduledDate());
		if (candidates.isEmpty()) return;

		WaitingList next = candidates.getFirst();
		next.setStatus(WaitingListStatus.NOTIFIED);
		next.setNotifiedAt(Instant.now());
		waitingListRepository.save(next);

		whatsAppMessageSender.notifyWaitingListSlot(
				next.getClient().getPhone(),
				next.getProfessional().getName(),
				next.getService().getName(),
				event.scheduledDate(),
				event.scheduledTime());
	}

	private WaitingList getAccessibleEntry(Long professionalId, Long waitingListId) {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		Object principal = authentication == null ? null : authentication.getPrincipal();

		if (principal instanceof AuthenticatedClient client) {
			return waitingListRepository.findByIdAndClientId(waitingListId, client.clientId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada na fila não encontrada"));
		}

		if (principal instanceof AuthenticatedProfessional) {
			professionalService.requireOwnership(professionalId);
			return waitingListRepository.findByIdAndProfessionalId(waitingListId, professionalId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada na fila não encontrada"));
		}

		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não autenticado");
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
