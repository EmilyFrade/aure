package com.aure.service;

import com.aure.api.dto.WaitingListResponseDto;
import com.aure.domain.WaitingList;
import com.aure.domain.WaitingListStatus;
import com.aure.messaging.AppointmentCancelledEvent;
import com.aure.messaging.WhatsAppMessageSender;
import com.aure.repository.WaitingListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class WaitingListService {

	private static final Set<WaitingListStatus> ACTIVE_STATUSES = Set.of(WaitingListStatus.WAITING, WaitingListStatus.NOTIFIED);

	private final WaitingListRepository waitingListRepository;
	private final ProfessionalService professionalService;
	private final WhatsAppMessageSender whatsAppMessageSender;

	@Transactional(readOnly = true)
	public List<WaitingListResponseDto> listForProfessional(Long professionalId) {
		professionalService.requireOwnership(professionalId);
		return waitingListRepository.findActiveByProfessional(professionalId, ACTIVE_STATUSES, LocalDate.now()).stream()
				.map(this::toResponse)
				.toList();
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
		List<WaitingList> candidates = waitingListRepository.findMatchingWaitingEntries(
				event.professionalId(), event.serviceId(), WaitingListStatus.WAITING, event.scheduledDate());
		if (candidates.isEmpty()) return;

		offer(candidates.getFirst(), event.scheduledDate(), event.scheduledTime());
	}

	private void offer(WaitingList entry, LocalDate date, LocalTime time) {
		entry.setStatus(WaitingListStatus.NOTIFIED);
		entry.setNotifiedAt(Instant.now());
		entry.setOfferedDate(date);
		entry.setOfferedTime(time);
		waitingListRepository.save(entry);

		whatsAppMessageSender.notifyWaitingListSlot(
				entry.getClient().getPhone(),
				entry.getProfessional().getName(),
				entry.getService().getName(),
				date,
				time);
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
}
