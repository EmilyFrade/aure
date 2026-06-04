package com.aure.api.controller;

import com.aure.api.dto.PublicProfileResponseDto;
import com.aure.service.PublicProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/p")
@RequiredArgsConstructor
public class PublicProfileController {

	private final PublicProfileService publicProfileService;

	@GetMapping("/{slug}")
	public PublicProfileResponseDto getBySlug(@PathVariable String slug) {
		return publicProfileService.getBySlug(slug);
	}
}
