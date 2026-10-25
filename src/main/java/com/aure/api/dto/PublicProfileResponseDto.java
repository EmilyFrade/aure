package com.aure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record PublicProfileResponseDto(
		@JsonProperty("brand_name") String brandName,
		String address,
		String city,
		String state,
		PublicProfessionalDto professional
) {
	public record PublicProfessionalDto(
			Long id,
			String name,
			String bio,
			String phone,
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
