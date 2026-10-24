package com.aure.service;

import com.aure.api.dto.ProfessionalProfileDto;
import com.aure.api.dto.ProfessionalProfileRequestDto;
import com.aure.domain.Professional;
import com.aure.domain.ProfessionalPhoto;
import com.aure.repository.ProfessionalPhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProfessionalProfileService {

	private static final Set<String> PHOTO_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
	private static final long MAX_PHOTO_BYTES = 1024 * 1024;

	private final ProfessionalService professionalService;
	private final ProfessionalPhotoRepository photoRepository;

	@Value("${aure.api-url}")
	private String apiUrl;

	@Transactional(readOnly = true)
	public ProfessionalProfileDto getProfile(Long professionalId) {
		professionalService.requireOwnership(professionalId);
		return ProfessionalProfileDto.from(professionalService.getProfessional(professionalId));
	}

	@Transactional
	public ProfessionalProfileDto updateProfile(Long professionalId, ProfessionalProfileRequestDto request) {
		professionalService.requireOwnership(professionalId);
		Professional professional = professionalService.getProfessional(professionalId);
		professional.setName(request.name().trim());
		professional.setBio(blankToNull(request.bio()));
		professional.setPhone(blankToNull(request.phone()));

		var brand = professional.getBrand();
		brand.setName(request.brandName() == null || request.brandName().isBlank() ? professional.getName() : request.brandName().trim());
		brand.setAddress(blankToNull(request.address()));
		brand.setCity(request.city().trim());
		brand.setState(request.state());

		return ProfessionalProfileDto.from(professional);
	}

	@Transactional
	public ProfessionalProfileDto updatePhoto(Long professionalId, MultipartFile file) {
		professionalService.requireOwnership(professionalId);
		if (file.isEmpty() || !PHOTO_TYPES.contains(file.getContentType())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envie uma imagem JPG, PNG ou WebP");
		}
		if (file.getSize() > MAX_PHOTO_BYTES) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A foto deve ter até 1 MB");
		}

		Professional professional = professionalService.getProfessional(professionalId);
		ProfessionalPhoto photo = photoRepository.findByProfessionalId(professionalId)
				.orElseGet(() -> ProfessionalPhoto.builder().professional(professional).build());
		try {
			photo.setContent(file.getBytes());
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não foi possível ler a foto");
		}
		photo.setContentType(file.getContentType());
		photo = photoRepository.saveAndFlush(photo);

		professional.setPhotoUrl(apiUrl + "/professionals/" + professionalId + "/photo?v=" + photo.getUpdatedAt().toEpochMilli());
		return ProfessionalProfileDto.from(professional);
	}

	@Transactional
	public ProfessionalProfileDto deletePhoto(Long professionalId) {
		professionalService.requireOwnership(professionalId);
		Professional professional = professionalService.getProfessional(professionalId);
		photoRepository.findByProfessionalId(professionalId).ifPresent(photoRepository::delete);
		professional.setPhotoUrl(null);
		return ProfessionalProfileDto.from(professional);
	}

	@Transactional(readOnly = true)
	public ProfessionalPhoto getPhoto(Long professionalId) {
		return photoRepository.findByProfessionalId(professionalId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Foto não encontrada"));
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
