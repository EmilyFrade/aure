package com.aure.api.dto;

import com.aure.domain.Brand;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponseDto {

	private Long id;
	private String name;
	private String description;

	@JsonProperty("logo_url")
	private String logoUrl;

	private String city;
	private String state;
	private String phone;
	private String email;
	private String website;

	public static BrandResponseDto from(Brand brand) {
		return new BrandResponseDto(
				brand.getId(),
				brand.getName(),
				brand.getDescription(),
				brand.getLogoUrl(),
				brand.getCity(),
				brand.getState(),
				brand.getPhone(),
				brand.getEmail(),
				brand.getWebsite()
		);
	}

}
