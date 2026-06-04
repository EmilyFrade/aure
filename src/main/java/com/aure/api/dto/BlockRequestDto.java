package com.aure.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlockRequestDto {

	@NotNull(message = "Início do bloqueio é obrigatório")
	@JsonProperty("start_datetime")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime startDatetime;

	@NotNull(message = "Fim do bloqueio é obrigatório")
	@JsonProperty("end_datetime")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
	private LocalDateTime endDatetime;

	@Size(max = 500)
	private String reason;
}
