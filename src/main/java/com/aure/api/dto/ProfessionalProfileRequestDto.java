package com.aure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfessionalProfileRequestDto(
		@NotBlank @Size(max = 255) String name,
		@Size(max = 500) String bio,
		@Size(max = 20) String phone,
		@NotBlank @Size(max = 255) @JsonProperty("brand_name") String brandName,
		@Size(max = 255) String address,
		@NotBlank @Size(max = 100) String city,
		@NotBlank @Pattern(regexp = "[A-Z]{2}") String state
) {
}
