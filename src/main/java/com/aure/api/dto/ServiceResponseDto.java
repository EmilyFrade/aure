package com.aure.api.dto;

import com.aure.domain.Service;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponseDto {

	private Long id;

	@JsonProperty("professional_id")
	private Long professionalId;

	private String name;
	
	private String description;

	@JsonProperty("duration_minutes")
	private int durationMinutes;

	private BigDecimal price;

	@JsonProperty("is_active")
	private boolean active;

	public static ServiceResponseDto from(Service service) {
		return new ServiceResponseDto(
			service.getId(),
			service.getProfessional().getId(),
			service.getName(),
			service.getDescription(),
			service.getDurationMinutes(),
			service.getPrice(),
			service.isActive()
		);
	}
}
