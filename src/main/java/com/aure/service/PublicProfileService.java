package com.aure.service;

import com.aure.api.dto.PublicProfileResponseDto;
import com.aure.api.dto.PublicProfileResponseDto.PublicProfessionalDto;
import com.aure.api.dto.PublicProfileResponseDto.PublicServiceDto;
import com.aure.domain.Professional;
import com.aure.repository.BrandRepository;
import com.aure.repository.ProfessionalRepository;
import com.aure.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicProfileService {

	private final BrandRepository brandRepository;
	private final ProfessionalRepository professionalRepository;
	private final ServiceRepository serviceRepository;

	@Transactional(readOnly = true)
	public PublicProfileResponseDto getBySlug(String slug) {
		var brand = brandRepository.findBySlug(slug)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil não encontrado"));

		List<PublicProfessionalDto> professionals = professionalRepository
				.findByBrandIdAndActiveTrue(brand.getId())
				.stream()
				.map(this::toPublicProfessionalDto)
				.toList();

		return new PublicProfileResponseDto(
				brand.getId(),
				brand.getName(),
				brand.getDescription(),
				brand.getCity(),
				brand.getState(),
				professionals
		);
	}

	private PublicProfessionalDto toPublicProfessionalDto(Professional professional) {
		List<PublicServiceDto> services = serviceRepository
				.findByProfessionalIdAndActiveTrue(professional.getId())
				.stream()
				.map(s -> new PublicServiceDto(s.getId(), s.getName(), s.getDescription(), s.getDurationMinutes(), s.getPrice()))
				.toList();

		return new PublicProfessionalDto(
				professional.getId(),
				professional.getName(),
				professional.getBio(),
				professional.getPhotoUrl(),
				services
		);
	}
}
