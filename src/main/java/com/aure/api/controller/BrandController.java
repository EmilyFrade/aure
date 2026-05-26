package com.aure.api.controller;

import com.aure.api.dto.BrandRequestDto;
import com.aure.api.dto.BrandResponseDto;
import com.aure.service.BrandService;
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
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandController {

	private final BrandService brandService;

	@GetMapping
	public List<BrandResponseDto> findAll() {
		return brandService.findAll();
	}

	@GetMapping("/{id}")
	public BrandResponseDto findById(@PathVariable Long id) {
		return brandService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BrandResponseDto create(@Valid @RequestBody BrandRequestDto request) {
		return brandService.create(request);
	}

	@PutMapping("/{id}")
	public BrandResponseDto update(@PathVariable Long id, @Valid @RequestBody BrandRequestDto request) {
		return brandService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		brandService.delete(id);
	}

}
