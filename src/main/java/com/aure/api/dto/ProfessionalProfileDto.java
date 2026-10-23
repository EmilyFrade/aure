package com.aure.api.dto;

import com.aure.domain.Professional;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ProfessionalProfileDto(
		String name,
		String bio,
		String phone,
		@JsonProperty("photo_url") String photoUrl,
		@JsonProperty("brand_name") String brandName,
		@JsonProperty("brand_slug") String brandSlug,
		String description,
		String address,
		String city,
		String state
) {

	public static ProfessionalProfileDto from(Professional professional) {
		var brand = professional.getBrand();
		return new ProfessionalProfileDto(
				professional.getName(),
				professional.getBio(),
				professional.getPhone(),
				professional.getPhotoUrl(),
				brand.getName(),
				brand.getSlug(),
				brand.getDescription(),
				brand.getAddress(),
				brand.getCity(),
				brand.getState());
	}
}
