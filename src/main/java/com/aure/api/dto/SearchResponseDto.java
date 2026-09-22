package com.aure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record SearchResponseDto(
		List<SearchResultDto> content,
		int page,
		int size,
		@JsonProperty("total_elements") long totalElements,
		@JsonProperty("total_pages") int totalPages
) {

	public record SearchResultDto(
			@JsonProperty("brand_id") Long brandId,
			@JsonProperty("brand_name") String brandName,
			String slug,
			String description,
			String city,
			String state,
			@JsonProperty("logo_url") String logoUrl,
			List<MatchedServiceDto> services
	) {}

	public record MatchedServiceDto(
			Long id,
			String name,
			@JsonProperty("duration_minutes") int durationMinutes,
			BigDecimal price
	) {}
}
