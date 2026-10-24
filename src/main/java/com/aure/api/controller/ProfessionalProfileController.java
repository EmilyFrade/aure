package com.aure.api.controller;

import com.aure.api.dto.ProfessionalProfileDto;
import com.aure.api.dto.ProfessionalProfileRequestDto;
import com.aure.domain.ProfessionalPhoto;
import com.aure.service.ProfessionalProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

@RestController
@RequestMapping("/professionals/{professionalId}")
@RequiredArgsConstructor
public class ProfessionalProfileController {

	private final ProfessionalProfileService profileService;

	@GetMapping("/profile")
	public ProfessionalProfileDto getProfile(@PathVariable Long professionalId) {
		return profileService.getProfile(professionalId);
	}

	@PutMapping("/profile")
	public ProfessionalProfileDto updateProfile(@PathVariable Long professionalId,
			@Valid @RequestBody ProfessionalProfileRequestDto request) {
		return profileService.updateProfile(professionalId, request);
	}

	@PutMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ProfessionalProfileDto updatePhoto(@PathVariable Long professionalId, @RequestPart("file") MultipartFile file) {
		return profileService.updatePhoto(professionalId, file);
	}

	@DeleteMapping("/photo")
	public ProfessionalProfileDto deletePhoto(@PathVariable Long professionalId) {
		return profileService.deletePhoto(professionalId);
	}

	@GetMapping("/photo")
	public ResponseEntity<byte[]> getPhoto(@PathVariable Long professionalId) {
		ProfessionalPhoto photo = profileService.getPhoto(professionalId);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(photo.getContentType()))
				.cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic())
				.body(photo.getContent());
	}
}
