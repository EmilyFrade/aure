package com.aure.api.controller;

import com.aure.api.dto.ServiceRequestDto;
import com.aure.api.dto.ServiceResponseDto;
import com.aure.service.ServiceCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/professionals/{professionalId}/services")
@RequiredArgsConstructor
public class ServiceController {

	private final ServiceCatalogService serviceCatalogService;

	@GetMapping
	public List<ServiceResponseDto> findAll(@PathVariable Long professionalId) {
		return serviceCatalogService.findAll(professionalId);
	}

	@GetMapping("/{serviceId}")
	public ServiceResponseDto findById(@PathVariable Long professionalId, @PathVariable Long serviceId) {
		return serviceCatalogService.findById(professionalId, serviceId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ServiceResponseDto create(@PathVariable Long professionalId, @Valid @RequestBody ServiceRequestDto request) {
		return serviceCatalogService.create(professionalId, request);
	}

	@PutMapping("/{serviceId}")
	public ServiceResponseDto update(
		@PathVariable Long professionalId,
		@PathVariable Long serviceId,
		@Valid @RequestBody ServiceRequestDto request
	) {
		return serviceCatalogService.update(professionalId, serviceId, request);
	}

	@DeleteMapping("/{serviceId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long professionalId, @PathVariable Long serviceId) {
		serviceCatalogService.delete(professionalId, serviceId);
	}
}
