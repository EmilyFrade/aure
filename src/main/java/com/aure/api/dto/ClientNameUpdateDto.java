package com.aure.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientNameUpdateDto(
		@NotBlank(message = "Nome é obrigatório")
		@Size(max = 120, message = "Nome muito longo")
		String name
) {
}
