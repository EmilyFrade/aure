package com.aure.api.controller;

import com.aure.api.dto.ProfessionalRequestDto;
import com.aure.api.dto.ProfessionalResponseDto;
import com.aure.service.ProfessionalService;
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
@RequestMapping("/professionals")
@RequiredArgsConstructor
public class ProfessionalController {

	private final ProfessionalService professionalService;

	@GetMapping
	public List<ProfessionalResponseDto> findAll() {
		return professionalService.findAll();
	}

	@GetMapping("/{id}")
	public ProfessionalResponseDto findById(@PathVariable Long id) {
		return professionalService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProfessionalResponseDto create(@Valid @RequestBody ProfessionalRequestDto request) {
		return professionalService.create(request);
	}

	@PutMapping("/{id}")
	public ProfessionalResponseDto update(@PathVariable Long id, @Valid @RequestBody ProfessionalRequestDto request) {
		return professionalService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		professionalService.delete(id);
	}

}
