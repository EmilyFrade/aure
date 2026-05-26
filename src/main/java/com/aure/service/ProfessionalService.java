package com.aure.service;

import com.aure.domain.Brand;
import com.aure.domain.Professional;
import com.aure.repository.BrandRepository;
import com.aure.repository.ProfessionalRepository;
import com.aure.api.dto.ProfessionalRequestDto;
import com.aure.api.dto.ProfessionalResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfessionalService {

	private final ProfessionalRepository professionalRepository;
	private final BrandRepository brandRepository;

	@Transactional(readOnly = true)
	public List<ProfessionalResponseDto> findAll() {
		return professionalRepository.findAll().stream()
				.map(ProfessionalResponseDto::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public ProfessionalResponseDto findById(Long id) {
		return ProfessionalResponseDto.from(getProfessional(id));
	}

	@Transactional
	public ProfessionalResponseDto create(ProfessionalRequestDto request) {
		Professional professional = mapToEntity(new Professional(), request);
		return ProfessionalResponseDto.from(professionalRepository.save(professional));
	}

	@Transactional
	public ProfessionalResponseDto update(Long id, ProfessionalRequestDto request) {
		Professional professional = getProfessional(id);
		mapToEntity(professional, request);
		return ProfessionalResponseDto.from(professionalRepository.save(professional));
	}

	@Transactional
	public void delete(Long id) {
		getProfessional(id);
		professionalRepository.deleteById(id);
	}

	private Professional mapToEntity(Professional professional, ProfessionalRequestDto request) {
		professional.setBrand(getBrand(request.getBrandId()));
		professional.setName(request.getName());
		professional.setBio(request.getBio());
		professional.setPhone(request.getPhone());
		professional.setEmail(request.getEmail());
		professional.setPhotoUrl(request.getPhotoUrl());
		if (request.getActive() != null) {
			professional.setActive(request.getActive());
		} else if (professional.getId() == null) {
			professional.setActive(true);
		}
		return professional;
	}

	private Brand getBrand(Long id) {
		return brandRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(
				HttpStatus.NOT_FOUND,
				"Marca não encontrada com id %d".formatted(id)
			));
	}

	private Professional getProfessional(Long id) {
		return professionalRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(
				HttpStatus.NOT_FOUND,
				"Profissional não encontrado com id %d".formatted(id)
			));
	}

}
