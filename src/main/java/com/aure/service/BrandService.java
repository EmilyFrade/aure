package com.aure.service;

import com.aure.domain.Brand;
import com.aure.repository.BrandRepository;
import com.aure.api.dto.BrandRequestDto;
import com.aure.api.dto.BrandResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {

	private final BrandRepository brandRepository;

	@Transactional(readOnly = true)
	public List<BrandResponseDto> findAll() {
		return brandRepository.findAll().stream()
				.map(BrandResponseDto::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public BrandResponseDto findById(Long id) {
		return BrandResponseDto.from(getBrand(id));
	}

	@Transactional
	public BrandResponseDto create(BrandRequestDto request) {
		if (brandRepository.findBySlug(request.getSlug()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug já está em uso");
		}

		Brand brand = mapToEntity(new Brand(), request);
		return BrandResponseDto.from(brandRepository.save(brand));
	}

	@Transactional
	public BrandResponseDto update(Long id, BrandRequestDto request) {
		Brand brand = getBrand(id);
		mapToEntity(brand, request);
		return BrandResponseDto.from(brandRepository.save(brand));
	}

	@Transactional
	public void delete(Long id) {
		getBrand(id);
		brandRepository.deleteById(id);
	}

	private Brand mapToEntity(Brand brand, BrandRequestDto request) {
		brand.setName(request.getName());
		brand.setSlug(request.getSlug());
		brand.setDescription(request.getDescription());
		brand.setLogoUrl(request.getLogoUrl());
		brand.setCity(request.getCity());
		brand.setState(request.getState());
		brand.setPhone(request.getPhone());
		brand.setEmail(request.getEmail());
		brand.setWebsite(request.getWebsite());
		return brand;
	}

	private Brand getBrand(Long id) {
		return brandRepository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(
				HttpStatus.NOT_FOUND,
				"Marca não encontrada com id %d".formatted(id)
			));
	}

}
