package com.aure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record PublicProfileResponseDto(
		@JsonProperty("brand_id") Long brandId,
		@JsonProperty("brand_name") String brandName,
		String description,
		String city,
		String state,
		List<PublicProfessionalDto> professionals
) {
	public record PublicProfessionalDto(
			Long id,
			String name,
			String bio,
			@JsonProperty("photo_url") String photoUrl,
			List<PublicServiceDto> services
	) {}

	public record PublicServiceDto(
			Long id,
			String name,
			String description,
			@JsonProperty("duration_minutes") int durationMinutes,
			BigDecimal price
	) {}
}
