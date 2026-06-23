package com.aure.service;

import com.aure.api.dto.AppointmentRequestDto;
import com.aure.api.dto.AppointmentResponseDto;
import com.aure.api.dto.AppointmentStatusUpdateDto;
import com.aure.api.dto.ManualAppointmentRequestDto;
import com.aure.domain.Appointment;
import com.aure.domain.AppointmentStatus;
import com.aure.domain.Client;
import com.aure.domain.Professional;
import com.aure.domain.Service;
import com.aure.messaging.AppointmentCancelledEvent;
import com.aure.messaging.AppointmentCreatedEvent;
import com.aure.messaging.AppointmentEventPublisher;
import com.aure.repository.AppointmentRepository;
import com.aure.repository.ClientRepository;
import com.aure.repository.ProfessionalRepository;
import com.aure.repository.ServiceRepository;
import com.aure.security.AuthenticatedClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
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
public class AppointmentService {

	private final AppointmentRepository appointmentRepository;
	private final ProfessionalRepository professionalRepository;
	private final ServiceRepository serviceRepository;
	private final ClientRepository clientRepository;
	private final ProfessionalService professionalService;
	private final AppointmentEventPublisher eventPublisher;

	@Transactional
	public AppointmentResponseDto createAppointment(AppointmentRequestDto request) {
		Client client = currentClient();

		Professional professional = professionalRepository.findById(request.professionalId())
				.filter(Professional::isActive)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profissional não encontrado"));

		Service service = serviceRepository.findByIdAndProfessionalId(request.serviceId(), professional.getId())
				.filter(Service::isActive)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));

		checkConflict(professional.getId(), request.scheduledDate(), request.scheduledTime(), service.getDurationMinutes());

		Appointment appointment = Appointment.builder()
				.professional(professional)
				.client(client)
				.service(service)
				.scheduledDate(request.scheduledDate())
				.scheduledTime(request.scheduledTime())
				.durationMinutes(service.getDurationMinutes())
				.price(service.getPrice())
				.status(AppointmentStatus.PENDING)
				.notes(request.notes())
				.build();

		appointment = appointmentRepository.save(appointment);

		eventPublisher.publish(new AppointmentCreatedEvent(
				appointment.getId(),
				professional.getId(),
				client.getId(),
				service.getId(),
				request.scheduledDate(),
				request.scheduledTime()
		));

		return appointmentRepository.findByIdWithDetails(appointment.getId()).map(AppointmentResponseDto::from).orElseThrow();
	}

	@Transactional(readOnly = true)
	public List<AppointmentResponseDto> listMyAppointments() {
		Client client = currentClient();
		return appointmentRepository.findByClientId(client.getId()).stream().map(AppointmentResponseDto::from).toList();
	}

	@Transactional
	public AppointmentResponseDto cancelByClient(Long appointmentId) {
		Client client = currentClient();
		Appointment appointment = appointmentRepository.findByIdAndClientId(appointmentId, client.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado"));
		return AppointmentResponseDto.from(cancel(appointment));
	}

	@Transactional
	public AppointmentResponseDto cancelByProfessional(Long professionalId, Long appointmentId) {
		professionalService.requireOwnership(professionalId);
		Appointment appointment = appointmentRepository.findByIdAndProfessionalId(appointmentId, professionalId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado"));
		return AppointmentResponseDto.from(cancel(appointment));
	}

	@Transactional(readOnly = true)
	public List<AppointmentResponseDto> listByProfessional(Long professionalId, LocalDate startDate, LocalDate endDate, AppointmentStatus status) {
		professionalService.requireOwnership(professionalId);
		return appointmentRepository.findByProfessionalIdWithFilters(professionalId, startDate, endDate, status)
				.stream().map(AppointmentResponseDto::from).toList();
	}

	@Transactional
	public AppointmentResponseDto createByProfessional(Long professionalId, ManualAppointmentRequestDto request) {
		professionalService.requireOwnership(professionalId);
		Professional professional = professionalService.getProfessional(professionalId);

		Service service = serviceRepository.findByIdAndProfessionalId(request.serviceId(), professionalId)
				.filter(Service::isActive)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));

		String phone = request.clientPhone().trim();
		Client client = clientRepository.findByPhone(phone)
				.orElseGet(() -> clientRepository.save(Client.builder().phone(phone).name(request.clientName()).build()));

		checkConflict(professionalId, request.scheduledDate(), request.scheduledTime(), service.getDurationMinutes());

		Appointment appointment = Appointment.builder()
				.professional(professional)
				.client(client)
				.service(service)
				.scheduledDate(request.scheduledDate())
				.scheduledTime(request.scheduledTime())
				.durationMinutes(service.getDurationMinutes())
				.price(service.getPrice())
				.status(AppointmentStatus.CONFIRMED)
				.confirmedAt(Instant.now())
				.notes(request.notes())
				.build();

		appointment = appointmentRepository.save(appointment);

		eventPublisher.publish(new AppointmentCreatedEvent(
				appointment.getId(),
				professionalId,
				client.getId(),
				service.getId(),
				request.scheduledDate(),
				request.scheduledTime()
		));

		return appointmentRepository.findByIdWithDetails(appointment.getId()).map(AppointmentResponseDto::from).orElseThrow();
	}

	private static final Set<AppointmentStatus> ALLOWED_MANUAL_STATUSES =
			Set.of(AppointmentStatus.CONFIRMED, AppointmentStatus.COMPLETED, AppointmentStatus.NO_SHOW);

	private static final Set<AppointmentStatus> TERMINAL_STATUSES =
			Set.of(AppointmentStatus.CANCELLED, AppointmentStatus.COMPLETED, AppointmentStatus.NO_SHOW);

	@Transactional
	public AppointmentResponseDto updateStatus(Long professionalId, Long appointmentId, AppointmentStatusUpdateDto request) {
		professionalService.requireOwnership(professionalId);

		if (!ALLOWED_MANUAL_STATUSES.contains(request.status())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Status inválido para esta operação; use /cancel para cancelar");
		}

		Appointment appointment = appointmentRepository.findByIdAndProfessionalId(appointmentId, professionalId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado"));

		if (TERMINAL_STATUSES.contains(appointment.getStatus())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Agendamento já está em um estado final");
		}

		switch (request.status()) {
			case CONFIRMED -> appointment.setConfirmedAt(Instant.now());
			case COMPLETED -> appointment.setCompletedAt(Instant.now());
			case NO_SHOW -> {}
			default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status inválido");
		}
		appointment.setStatus(request.status());

		return AppointmentResponseDto.from(appointmentRepository.save(appointment));
	}

	private Appointment cancel(Appointment appointment) {
		if (TERMINAL_STATUSES.contains(appointment.getStatus())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Agendamento não pode mais ser cancelado");
		}

		appointment.setStatus(AppointmentStatus.CANCELLED);
		appointment.setCancelledAt(Instant.now());
		appointment = appointmentRepository.save(appointment);

		eventPublisher.publish(new AppointmentCancelledEvent(
				appointment.getId(),
				appointment.getProfessional().getId(),
				appointment.getClient().getId(),
				appointment.getService().getId(),
				appointment.getScheduledDate(),
				appointment.getScheduledTime()
		));

		return appointment;
	}

	private void checkConflict(Long professionalId, LocalDate scheduledDate, LocalTime newStart, int durationMinutes) {
		LocalTime newEnd = newStart.plusMinutes(durationMinutes);

		boolean hasConflict = appointmentRepository
				.findByProfessionalIdAndScheduledDateAndStatusNot(
						professionalId, scheduledDate, AppointmentStatus.CANCELLED)
				.stream()
				.anyMatch(existing -> {
					LocalTime existingStart = existing.getScheduledTime();
					LocalTime existingEnd = existingStart.plusMinutes(existing.getDurationMinutes());
					return newStart.isBefore(existingEnd) && newEnd.isAfter(existingStart);
				});

		if (hasConflict) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Horário indisponível");
		}
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
