package com.aure.api.dto;

import com.aure.domain.Professional;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalResponseDto {

	private Long id;

	@JsonProperty("brand_id")
	private Long brandId;

	private String name;
	private String bio;
	private String phone;
	private String email;

	@JsonProperty("photo_url")
	private String photoUrl;

	@JsonProperty("is_active")
	private boolean active;

	public static ProfessionalResponseDto from(Professional professional) {
		return new ProfessionalResponseDto(
				professional.getId(),
				professional.getBrand().getId(),
				professional.getName(),
				professional.getBio(),
				professional.getPhone(),
				professional.getEmail(),
				professional.getPhotoUrl(),
				professional.isActive()
		);
	}

}
