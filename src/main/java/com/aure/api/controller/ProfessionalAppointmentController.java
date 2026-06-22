package com.aure.api.controller;

import com.aure.api.dto.AppointmentResponseDto;
import com.aure.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/professionals/{professionalId}/appointments")
@RequiredArgsConstructor
public class ProfessionalAppointmentController {

	private final AppointmentService appointmentService;

	@GetMapping
	public List<AppointmentResponseDto> findAll(@PathVariable Long professionalId) {
		return appointmentService.listByProfessional(professionalId);
	}

	@PostMapping("/{appointmentId}/cancel")
	public AppointmentResponseDto cancel(@PathVariable Long professionalId, @PathVariable Long appointmentId) {
		return appointmentService.cancelByProfessional(professionalId, appointmentId);
	}
}
