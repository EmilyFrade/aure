package com.aure.api.controller;

import com.aure.api.dto.AppointmentResponseDto;
import com.aure.api.dto.AppointmentStatusUpdateDto;
import com.aure.api.dto.ManualAppointmentRequestDto;
import com.aure.domain.AppointmentStatus;
import com.aure.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/professionals/{professionalId}/appointments")
@RequiredArgsConstructor
public class ProfessionalAppointmentController {

	private final AppointmentService appointmentService;

	@GetMapping
	public List<AppointmentResponseDto> findAll(
			@PathVariable Long professionalId,
			@RequestParam(name = "start_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(name = "end_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(required = false) AppointmentStatus status) {
		return appointmentService.listByProfessional(professionalId, startDate, endDate, status);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AppointmentResponseDto create(@PathVariable Long professionalId, @Valid @RequestBody ManualAppointmentRequestDto request) {
		return appointmentService.createByProfessional(professionalId, request);
	}

	@PostMapping("/{appointmentId}/cancel")
	public AppointmentResponseDto cancel(@PathVariable Long professionalId, @PathVariable Long appointmentId) {
		return appointmentService.cancelByProfessional(professionalId, appointmentId);
	}

	@PatchMapping("/{appointmentId}/status")
	public AppointmentResponseDto updateStatus(
			@PathVariable Long professionalId,
			@PathVariable Long appointmentId,
			@Valid @RequestBody AppointmentStatusUpdateDto request) {
		return appointmentService.updateStatus(professionalId, appointmentId, request);
	}
}
