package com.aure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequestDto {

	@NotBlank(message = "Nome é obrigatório")
	@Size(max = 255)
	private String name;

	@Size(max = 500)
	private String description;

	@NotNull(message = "Duração é obrigatória")
	@Min(value = 1, message = "Duração mínima é 1 minuto")
	@JsonProperty("duration_minutes")
	private Integer durationMinutes;

	@NotNull(message = "Preço é obrigatório")
	@DecimalMin(value = "0.0", message = "Preço deve ser maior ou igual a zero")
	private BigDecimal price;

	@JsonProperty("is_active")
	private Boolean active;
}
