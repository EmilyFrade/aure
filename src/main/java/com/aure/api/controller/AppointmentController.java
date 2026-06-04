package com.aure.api.controller;

import com.aure.api.dto.AppointmentRequestDto;
import com.aure.api.dto.AppointmentResponseDto;
import com.aure.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

	private final AppointmentService appointmentService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AppointmentResponseDto create(@Valid @RequestBody AppointmentRequestDto request) {
		return appointmentService.createAppointment(request);
	}

	@GetMapping
	public List<AppointmentResponseDto> listMine() {
		return appointmentService.listMyAppointments();
	}
}
